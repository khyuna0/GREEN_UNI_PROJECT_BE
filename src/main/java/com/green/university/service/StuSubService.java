package com.green.university.service;

import com.green.university.dto.response.StuSubAppDto;
import com.green.university.dto.response.StuSubDayTimeDto;
import com.green.university.dto.response.StuSubSumGradesDto;
import com.green.university.entity.*;
import com.green.university.exception.CustomRestfullException;
import com.green.university.repository.interfaces.*;
import com.green.university.utils.Define;
import com.green.university.utils.StuSubUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public StuSub readStuSub(Long studentId, Long subjectId) {
        return stuSubRepository.findByStudent_IdAndSubject_Id(studentId, subjectId).orElseThrow(
                () -> new CustomRestfullException("학생 과목 정보 없음", HttpStatus.NOT_FOUND)
        );
    }

    // 학생의 해당 학기 수강신청 내역 조회
    public List<StuSubAppDto> readStuSubList(Long studentId) {
        List<StuSub> stuSubList = stuSubRepository.findByStudent_IdAndSubject_SubYearAndSubject_Semester(
                studentId, Define.CURRENT_YEAR, Define.CURRENT_SEMESTER);
        return stuSubList.stream()
                .map(StuSubAppDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 학생의 수강신청 내역 추가
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

        // 신청 대상 과목의 정원이 다 찼다면 신청 불가
        if (targetSubject.getNumOfStudent() >= targetSubject.getCapacity()) {
            throw new CustomRestfullException("정원이 초과되었습니다.", HttpStatus.NOT_FOUND);
        }

        // 이번 학기 과목인지 확인!
        if (!targetSubject.getSubYear().equals(Define.CURRENT_YEAR) ||
                !targetSubject.getSemester().equals(Define.CURRENT_SEMESTER)) {
            throw new CustomRestfullException("이번 학기 과목만 신청 가능", HttpStatus.BAD_REQUEST);
        }

        List<StuSub> stuSubList = stuSubRepository.findByStudent_IdAndSubject_SubYearAndSubject_Semester(
                studentId, Define.CURRENT_YEAR, Define.CURRENT_SEMESTER);
        // 1. 현재 총 신청 학점 계산
        // StuSub 리스트 → Subject 학점 숫자들 → grade 총합
        Long currentTotalGrade = stuSubList.stream()
                .mapToLong(stuSub -> stuSub.getSubject().getGrades())
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

        StuSub stuSub = stuSubRepository.findByStudent_IdAndSubject_Id(studentId, subjectId).orElseThrow(
                () -> new CustomRestfullException("학생 과목 정보 없음", HttpStatus.NOT_FOUND)
        );
        StuSubDetail stuSubDetail = stuSubDetailRepository.findByStuSub(stuSub).orElseThrow(
                () -> new CustomRestfullException("학생 과목 정보 없음", HttpStatus.NOT_FOUND)
        );
        stuSub.setSubject(targetSubject);
        stuSub.setStudent(targetStudent);
        stuSubDetail.setStuSub(stuSub);
        stuSubRepository.save(stuSub); // 수강신청 내역 추가
        stuSubDetailRepository.save(stuSubDetail); // 수강 상세 내역에도 데이터 추가

        // 해당 강의 현재인원 +1
        subjectService.updatePlusNumOfStudent(subjectId);
    }

    // 학생의 수강신청 내역 삭제
    @Transactional
    public void deleteStuSub(Long studentId, Long subjectId) {
        // 수강신청 내역 삭제
        stuSubRepository.deleteByStudent_IdAndSubject_Id(studentId, subjectId);
        // 해당 강의 현재인원 -1
        subjectService.updateMinusNumOfStudent(subjectId);
    }

    // 예비 수강 신청 기간 -> 수강 신청 기간 변경 시 로직
    @Transactional
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
        for (
                Long subjectId : idList2) {
            // 강의 엔티티 조회 후 현재 인원 초기화
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
            subject.setNumOfStudent(0L);
        }
    }

    // 수강 신청 내역과 예비 수강 신청 내역 조인 후 조회 -> 예비 수강 신청에만 존재
    @Transactional
    public List<StuSubAppDto> readPreStuSubByStuSub(Long studentId) {
        List<StuSub> stuSubList = stuSubRepository.findByStudent_Id(studentId);
        return stuSubList.stream().map(StuSubAppDto::fromEntity).collect(Collectors.toList());
    }

    // 점수 입력 시 F면 취득학점 0, F가 아니면 강의의 이수학점
    @Transactional
    public void updateCompleteGrade(Long studentId, Long subjectId, Long completeGrade) {
        StuSub stuSub = new StuSub();
        Grade grade = new Grade();
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(
                () -> new CustomRestfullException("과목을 찾을 수 없음", HttpStatus.NOT_FOUND)
        );

        if (stuSub.getGrade().getGrade().equals("F")) {
            grade.setGradeValue(0L);
        } else {
            grade.setGradeValue(subject.getGrades());
        }
        stuSub.setGrade(grade);
        stuSub.setCompleteGrade(completeGrade);
        stuSubRepository.save(stuSub);
    }
}
