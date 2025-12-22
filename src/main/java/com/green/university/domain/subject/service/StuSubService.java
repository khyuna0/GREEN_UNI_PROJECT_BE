package com.green.university.domain.subject.service;

import com.green.university.domain.grade.entity.Grade;
import com.green.university.domain.student.entity.Student;
import com.green.university.domain.student.repository.StudentRepository;
import com.green.university.domain.subject.dto.StuSubAppDto;
import com.green.university.domain.subject.dto.StuSubDayTimeDto;
import com.green.university.domain.subject.dto.StuSubSumGradesDto;
import com.green.university.domain.subject.dto.TimetableCourseDto;
import com.green.university.domain.subject.entity.PreStuSub;
import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.entity.StuSubDetail;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.domain.subject.repository.PreStuSubRepository;
import com.green.university.domain.subject.repository.StuSubDetailRepository;
import com.green.university.domain.subject.repository.StuSubRepository;
import com.green.university.domain.subject.repository.SubjectRepository;
import com.green.university.domain.subject.specification.SubjectSpecification;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.utils.Define;
import com.green.university.global.utils.StuSubUtil;
import com.green.university.global.utils.TermUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 서영
 */
@Service
public class StuSubService {

    @Autowired
    private StuSubRepository stuSubRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private PreStuSubRepository preStuSubRepository;
    @Autowired
    private StuSubDetailRepository stuSubDetailRepository;
    @Autowired
    private StudentRepository studentRepository;

    // 학생의 수강신청 내역에 해당 강의가 존재하는지 확인
    public Optional<StuSub> readStuSub(Long studentId, Long subjectId) {
        return stuSubRepository.findByStudent_IdAndSubject_Id(studentId, subjectId);
    }

    // 🔥🔥 학생의 해당 학기 수강 신청 내역 조회
    public List<StuSubAppDto> readStuSubList(Long studentId) {
        List<StuSub> stuSubList = stuSubRepository.findByStudent_IdAndSubject_SubYearAndSubject_Semester(
                studentId, TermUtil.currentYear(), TermUtil.currentSemester());
        return stuSubList.stream()
                .map(StuSubAppDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 🔥🔥 학생의 수강 신청 내역 추가
    @Transactional
    public void createStuSub(Long studentId, Long subjectId) {
        // 신청 학생 정보
        Student targetStudent = studentRepository.findById(studentId).orElseThrow(
                () -> new CustomRestfullException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        );
        // 신청 대상 과목 정보
        Subject targetSubject = subjectRepository.findById(subjectId).orElseThrow(
                () -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        );

        // 중복 체크
        Optional<StuSub> stuSubOptional = stuSubRepository.findByStudent_IdAndSubject_Id(studentId, subjectId);
        if (stuSubOptional.isPresent()) {
            throw new CustomRestfullException("이미 수강 신청한 과목입니다.", HttpStatus.BAD_REQUEST);
        }

        // 신청 대상 과목의 정원이 다 찼다면 신청 불가
        if (targetSubject.getNumOfStudent() >= targetSubject.getCapacity()) {
            throw new CustomRestfullException("정원이 초과되었습니다.", HttpStatus.NOT_FOUND);
        }

        // 이번 학기 과목인지 확인!
        if (!targetSubject.getSubYear().equals(TermUtil.currentYear()) ||
                !targetSubject.getSemester().equals(TermUtil.currentSemester())) {
            throw new CustomRestfullException("이번 학기 과목만 신청 가능", HttpStatus.BAD_REQUEST);
        }

        List<StuSub> stuSubList = stuSubRepository.findByStudent_IdAndSubject_SubYearAndSubject_Semester(
                studentId, TermUtil.currentYear(), TermUtil.currentSemester());
        // 1. 현재 총 신청 학점 계산
        // StuSub 리스트 → Subject 학점 숫자들 → grade 총합
        Long currentTotalGrade = stuSubList.stream()
                .mapToLong(stuSub -> stuSub.getSubject().getCredits())
                .sum();

        StuSubSumGradesDto stuSubSumGradesDto = new StuSubSumGradesDto();
        stuSubSumGradesDto.setStudentId(studentId);
        stuSubSumGradesDto.setSumGrades(currentTotalGrade);

        // 2. 해당 학생의 예비 수강 신청 내역 시간표 (시간표 DTO 리스트)
        List<StuSubDayTimeDto> dayTimeList = stuSubList.stream()
                .map(StuSubDayTimeDto::fromEntity)
                .toList();

        // 최대 수강 가능 학점을 넘지 않는지 + 현재 학생의 시간표와 겹치지 않는지 확인
        StuSubUtil.checkSumGrades(targetSubject, stuSubSumGradesDto);
        StuSubUtil.checkDayTime(targetSubject, dayTimeList);


        StuSub stuSub = new StuSub();
        stuSub.setSubject(targetSubject);
        stuSub.setStudent(targetStudent);
        stuSubRepository.save(stuSub); // 수강신청 내역 추가

        // 해당 강의 현재인원 +1
        subjectService.updatePlusNumOfStudent(subjectId);
    }

    // 🔥🔥 학생의 수강신청 내역 삭제
    @Transactional
    public void deleteStuSub(Long studentId, Long subjectId) {
        StuSub stuSub = stuSubRepository.findByStudent_IdAndSubject_Id(studentId, subjectId)
                .orElseThrow(() -> new CustomRestfullException("수강 신청 내역이 없습니다.", HttpStatus.NOT_FOUND));
        // 수강신청 내역 삭제
        stuSubRepository.deleteById(stuSub.getId());
        // 해당 강의 현재인원 -1
        subjectService.updateMinusNumOfStudent(subjectId);
    }

    // 🔥🔥🔥 예비 수강 신청 기간 -> 수강 신청 기간 변경 시 로직 (ai 도움 받음)
    @Transactional
    public void movePreToStuSubBatch() {
        System.out.println("========== 배치 시작 ==========");

        // 1. 특정 연도, 학기 강의 조회
        //List<Subject> allSubjects = subjectRepository.findAll();
        Specification<Subject> spec = SubjectSpecification.currentSemester(
                TermUtil.currentYear(),
                TermUtil.currentSemester()
        );
        List<Subject> currentSubjects = subjectRepository.findAll(spec);
        System.out.println("📚 현재 학기 과목 수: " + currentSubjects.size());

        // 2. 정원 초과 여부 확인
        Map<Long, Boolean> subjectOverCapacity = new HashMap<>();
        for (Subject subject : currentSubjects) {
            boolean isOverCapacity = subject.getNumOfStudent() > subject.getCapacity();
            subjectOverCapacity.put(subject.getId(), isOverCapacity);

            // 정원 초과된 과목은 신청 인원 0으로 초기화
            if (isOverCapacity) {
                System.out.println("⚠️ 정원 초과 과목: " + subject.getName() +
                        " (신청:" + subject.getNumOfStudent() + " / 정원:" + subject.getCapacity() + ")");
                subject.setNumOfStudent(0L);
                subjectRepository.save(subject);
            }
        }

        // 3. 모든 예비 수강 신청 조회
        List<PreStuSub> allPre = preStuSubRepository.findAll();
        System.out.println("📋 전체 예비 수강 신청 건수: " + allPre.size());

        int successCount = 0;
        int failCount = 0;

        for (PreStuSub pre : allPre) {
            Student student = pre.getStudent();
            Subject subject = pre.getSubject();

            // 현재 학기 과목이 아니면 스킵 (안전장치)
            if (!subject.getSubYear().equals(TermUtil.currentYear()) ||
                    !subject.getSemester().equals(TermUtil.currentSemester())) {
                System.out.println("⏭️ 현재 학기가 아님: " + subject.getName() +
                        " (학생ID: " + student.getId() + ")");
                continue;
            }

            // 해당 과목이 정원 초과였다면 → 예비는 남겨둠 (재신청 가능)
            if (subjectOverCapacity.getOrDefault(subject.getId(), false)) {
                System.out.println("❌ 과목 정원 초과로 자동 이동 실패: " + subject.getName() +
                        " (학생ID: " + student.getId() + ")");
                failCount++;
                continue;
            }

            // 현재 정원 체크
            if (subject.getNumOfStudent() >= subject.getCapacity()) {
                System.out.println("❌ 현재 정원 초과: " + subject.getName() +
                        " (학생ID: " + student.getId() + ")");
                failCount++;
                continue;
            }

            // 이미 수강 신청되어 있으면 스킵
            if (stuSubRepository.findByStudent_IdAndSubject_Id(student.getId(), subject.getId()).isPresent()) {
                System.out.println("⏭️ 이미 수강 신청됨: " + subject.getName() +
                        " (학생ID: " + student.getId() + ")");
                preStuSubRepository.delete(pre); // 예비는 삭제
                continue;
            }

            try {
                // StuSub 생성
                StuSub stuSub = new StuSub();
                stuSub.setStudent(student);
                stuSub.setSubject(subject);
                stuSubRepository.save(stuSub);

                // 과목 현재 인원
                subject.setNumOfStudent(subject.getNumOfStudent());
                subjectRepository.save(subject);

                // 예비 삭제
                preStuSubRepository.delete(pre);

                System.out.println("✅ 자동 이동 성공: " + subject.getName() +
                        " (학생ID: " + student.getId() + ")");
                successCount++;

            } catch (Exception e) {
                System.err.println("❌ 자동 이동 실패: " + subject.getName() +
                        " (학생ID: " + student.getId() + ") - " + e.getMessage());
                failCount++;
            }
        }

        System.out.println("========== 배치 완료 ==========");
        System.out.println("성공: " + successCount + "건, 실패: " + failCount + "건");
    }

    /**
     public void movePreToStuSubBatch() {
     System.out.println("========== 배치 시작 ==========");

     // 1. 모든 예비 수강신청 내역 가져오기
     List<PreStuSub> allPre = preStuSubRepository.findAll();
     System.out.println("[불3] allpre: " + allPre);
     for (PreStuSub pre : allPre) {
     Student student = pre.getStudent();
     Subject subject = pre.getSubject();

     // 2. 정원 체크
     if (subject.getNumOfStudent() >= subject.getCapacity()) {
     // 정원 초과 → 신청 인원 0으로 초기화 (과목 단위로 한 번만)
     subject.setNumOfStudent(0L);
     // 예비는 남겨둠 (학생이 다시 신청할 수 있게)
     continue;
     }

     // 3. 이미 같은 과목 수강신청되어 있으면 스킵 (안전 장치)
     if (stuSubRepository.findByStudent_IdAndSubject_Id(student.getId(), subject.getId()).isPresent()) {
     continue;
     }

     // 4. StuSub 생성
     StuSub stuSub = new StuSub();
     stuSub.setStudent(student);
     stuSub.setSubject(subject);
     stuSubRepository.save(stuSub);
     System.out.println("[불3] stuSub: " + stuSub);
     // 5. 상세 내역도 필요하면 같이 생성
     StuSubDetail detail = new StuSubDetail();
     detail.setStuSub(stuSub);
     stuSubDetailRepository.save(detail);
     System.out.println("[불3] detail: " + detail);
     // 6. 과목 현재 인원 +1
     subject.setNumOfStudent(subject.getNumOfStudent() + 1);

     // 7. 예비 수강신청 삭제
     preStuSubRepository.delete(pre);
     }
     }
     */

    /**
     public void createStuSubByPreStuSub() {
     // 1. 정원 >= 신청인원인 강의
     List<Long> idList1 = subjectRepository.findIdByCapacityGreaterThanOrEqualNumOfStudent();
     for (Long subjectId : idList1) {
     // 예비 수강 신청에서 해당 강의를 신청했던 내역 가져오기
     List<PreStuSub> preAppList = preStuSubRepository.findBySubject_Id(subjectId);
     // 예비 수강 신청했던 인원들이 자동으로 수강 신청되도록 해당 내역 그대로 수강 신청 추가
     for (PreStuSub pss : preAppList) {
     StuSub stuSub = stuSubRepository.findByStudent_IdAndSubject_Id(
     pss.getStudent().getId(), pss.getSubject().getId()).orElseThrow(
     () -> new CustomRestfullException("학생 과목 정보 없음", HttpStatus.NOT_FOUND)
     );
     stuSub.setStudent(pss.getStudent());
     stuSub.setSubject(pss.getSubject());
     stuSubRepository.save(stuSub);

     StuSubDetail stuSubDetail = stuSubDetailRepository.findByStuSub(stuSub).orElseThrow(
     () -> new CustomRestfullException("학생 과목 정보 없음", HttpStatus.NOT_FOUND)
     );
     stuSubDetailRepository.save(stuSubDetail); // 수강 상세 내역에도 데이터 추가
     }
     }

     // 2. 정원 < 신청인원인 강의
     List<Long> idList2 = subjectRepository.findIdByCapacityLessThanNumOfStudent();
     for (Long subjectId : idList2) {
     // 강의 엔티티 조회 후 현재 인원 초기화
     Subject subject = subjectRepository.findById(subjectId)
     .orElseThrow(() -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
     subject.setNumOfStudent(0L);
     }
     }
     */

    // 학생의 예비 수강 신청 중 아직 본 수강으로 안 넘어간 목록
    public List<StuSubAppDto> readPreStuSubByStuSub(Long studentId) {
        // 예비 수강 신청 테이블에 남아있는 것들만 조회
        List<PreStuSub> preStuSubList = preStuSubRepository.findByStudent_Id(studentId);
        for (PreStuSub pre : preStuSubList) {
            pre.setStatus(false);
        }
        return preStuSubList.stream()
                .map(pre -> new StuSubAppDto(
                        studentId,
                        pre.getSubject(),
                        pre.getSubject().getProfessor(),
                        false
                ))
                .collect(Collectors.toList());
    }

    // 점수 입력 시 F면 취득학점 0, F가 아니면 강의의 이수학점
    @Transactional
    public void updateCreditsFromLetterGrade(Long studentId, Long subjectId) {

        // 기존 StuSub 조회
        StuSub stuSub = stuSubRepository
                .findByStudent_IdAndSubject_Id(studentId, subjectId)
                .orElseThrow(() -> new CustomRestfullException("수강 정보 없음", HttpStatus.NOT_FOUND));

        // 연관된 Grade 조회
        Grade grade = stuSub.getLetterGrade();
        if (grade == null) {
            throw new CustomRestfullException("등급이 먼저 설정되어야 합니다.", HttpStatus.BAD_REQUEST);
        }

        // 3. 과목 조회 (학점 가져오기)
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new CustomRestfullException("과목을 찾을 수 없음", HttpStatus.NOT_FOUND));

        // 4. F 학점이면 0점, 아니면 subject.credits 적용
        if ("F".equals(grade.getLetterGrade())) {
            stuSub.setCredits(0L);
        } else {
            stuSub.setCredits(subject.getCredits());
        }

        // 5. 저장
        stuSubRepository.save(stuSub);
    }

    // 🔥🔥 수강 신청 기간 -> 수강 신청 기간 종료 변경 시 로직
    @Transactional
    public void moveStuSubToDetailBatch() {
        // pre에 있는 모든 데이터 지우기
        preStuSubRepository.deleteAll();
        // stusub에서 데이터를 가져와서
        List<StuSub> all = stuSubRepository.findAll();
        for (StuSub stuSub : all) {
            Optional<StuSubDetail> detail = stuSubDetailRepository.findByStuSub(stuSub);
            if (detail.isPresent()) {
                continue;
            }
            // detail에 3가지를 넣어주고 save 하기
            StuSubDetail stuSubDetail = new StuSubDetail();
            stuSubDetail.setStuSub(stuSub);
            stuSubDetail.setStudent(stuSub.getStudent());
            stuSubDetail.setSubject(stuSub.getSubject());
            stuSubDetailRepository.save(stuSubDetail);
        }
    }

    // 최종 시간표 테이블용 dto 변환
    @Transactional
    public List<TimetableCourseDto> readMyTimetable(Long studentId) {
        return readStuSubList(studentId).stream()
                .map(TimetableCourseDto::from)
                .collect(Collectors.toList());
    }
}
