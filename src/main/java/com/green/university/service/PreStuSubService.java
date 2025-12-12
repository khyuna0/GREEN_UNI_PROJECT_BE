package com.green.university.service;

import com.green.university.dto.response.StuSubAppDto;
import com.green.university.dto.response.StuSubDayTimeDto;
import com.green.university.dto.response.StuSubSumGradesDto;
import com.green.university.exception.CustomRestfullException;
import com.green.university.repository.PreStuSubRepository;
import com.green.university.repository.StudentRepository;
import com.green.university.repository.SubjectRepository;
import com.green.university.entity.PreStuSub;
import com.green.university.entity.Subject;
import com.green.university.utils.StuSubUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 서영
 */
@Service
public class PreStuSubService {

    @Autowired
    private PreStuSubRepository preStuSubRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private StudentRepository studentRepository;

    // 🔥 학생의 예비 수강신청 내역에 해당 강의가 존재하는지 확인
    public PreStuSub readPreStuSub(Long studentId, Long subjectId) {
        return preStuSubRepository.findByStudent_IdAndSubject_Id(studentId, subjectId);
    }

    // 🔥 학생의 전체 예비 수강신청 내역 조회
    public List<StuSubAppDto> readPreStuSubList(Long studentId) {
        return preStuSubRepository.findByStudent_Id(studentId)
                .stream() // 리스트 내부의 각 preStuSub 엔티티(pre)를 StuSubAppDto 객체로 변환
                .map(pre -> new StuSubAppDto(
                        studentId,
                        pre.getSubject(),
                        pre.getSubject().getProfessor()
                ))
                .toList();
    }

    // 🔥 학생의 예비 수강신청 내역 추가
    @Transactional
    public void createPreStuSub(Long studentId, Long subjectId) {
        // 1. 이미 신청한 과목인지 확인
        PreStuSub existed = readPreStuSub(studentId, subjectId);
        if (existed != null) {
            throw new CustomRestfullException(
                    "이미 해당 과목을 예비 수강신청했습니다.", HttpStatus.BAD_REQUEST);
        }

        // 2. 신청 대상 과목 정보
        Subject targetSubject = subjectRepository.findById(subjectId).orElseThrow(
                () -> new CustomRestfullException("없는 과목입니다.", HttpStatus.NOT_FOUND));

        // 3. 현재 총 예비 학점 계산
        List<PreStuSub> preStuSubList = preStuSubRepository.findByStudent_Id(studentId);
        Long totalGrades = preStuSubRepository.findByStudent_Id(studentId)
                .stream() //리스트를 스트림(Stream)으로 바꿔서 반복 처리
                // PreStuSub에서 Subject를 꺼내고, 그 Subject의 학점(grades, 타입 Long)을 long 형태로 변환해 추출
                .mapToLong(pre -> pre.getSubject().getGrades())
                .sum();

        // 현재 총 신청 학점 검증용 DTO
        StuSubSumGradesDto stuSubSumGradesDto = new StuSubSumGradesDto();
        stuSubSumGradesDto.setSumGrades(totalGrades); // 총학점 저장
        stuSubSumGradesDto.setStudentId(studentId);
        // 최대 수강 가능 학점을 넘지 않는지 확인
        StuSubUtil.checkSumGrades(targetSubject, stuSubSumGradesDto);

        // 4. 해당 학생의 예비 수강 신청 내역 시간표
        List<StuSubDayTimeDto> dayTimeList = preStuSubList.stream()
                .map(pre -> new StuSubDayTimeDto(pre.getSubject())) // 시간 정보만 DTO에 저장하고
                .toList(); // StuSubDayTimeDto 리스트로 반환

        // 5. 현재 학생의 시간표와 겹치지 않는지 확인
        StuSubUtil.checkDayTime(targetSubject, dayTimeList);

        // 6. 예비 수강신청 내역 저장
        PreStuSub preStuSub = new PreStuSub();
        preStuSub.setStudent(studentRepository.findById(studentId).orElseThrow(
                () -> new CustomRestfullException("없는 학생 정보입니다.", HttpStatus.NOT_FOUND)));
        preStuSub.setSubject(targetSubject);
        preStuSubRepository.save(preStuSub);

        // 7. 해당 강의 현재인원 +1
        targetSubject.setNumOfStudent(targetSubject.getNumOfStudent() + 1);

    }

    // 🔥 학생의 예비 수강신청 내역 삭제
    @Transactional
    public void deletePreStuSub(Long studentId, Long subjectId) {
        PreStuSub preStuSub = preStuSubRepository.findByStudent_IdAndSubject_Id(studentId, subjectId);
        if (preStuSub == null) { // 해당 예비 수강 신청 내역 없을 때 예외처리
            throw new CustomRestfullException("신청 내역이 존재하지 않습니다.", HttpStatus.NOT_FOUND);
        }
        preStuSubRepository.deleteById(preStuSub.getId());
        // 해당 강의 현재인원 -1
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(
                () -> new CustomRestfullException("없는 과목입니다.", HttpStatus.NOT_FOUND));
        subject.setNumOfStudent(subject.getNumOfStudent() - 1);
    }

    @Transactional
    public void deleteBySubject_Id(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(
                () -> new CustomRestfullException("과목을 찾을 수 없습니다", HttpStatus.NOT_FOUND)
        );
        preStuSubRepository.deleteBySubject_Id(subjectId);
    }
}
