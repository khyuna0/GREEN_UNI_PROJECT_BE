package com.green.university.domain.counseling.service;

import com.green.university.domain.counseling.dto.CounselingPreReserveDto;
import com.green.university.domain.counseling.dto.PreReserveDto;
import com.green.university.domain.counseling.entity.CounselingPreReserve;
import com.green.university.domain.counseling.repository.CounselingPreReserveRepository;
import com.green.university.domain.counseling.repository.CounselingScheduleRepository;
import com.green.university.domain.student.entity.Student;
import com.green.university.domain.student.repository.StudentRepository;
import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.domain.subject.repository.StuSubRepository;
import com.green.university.domain.subject.repository.SubjectRepository;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.infra.ai.DropoutRiskRepository;
import com.green.university.infra.ai.entity.DropoutRisk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CounselingPreReserveService {

    @Autowired
    private CounselingPreReserveRepository counselingPreReserveRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private StuSubRepository stuSubRepository;
    @Autowired
    private CounselingScheduleRepository counselingScheduleRepository;
    @Autowired
    private DropoutRiskRepository dropoutRiskRepository;
    @Autowired
    private SubjectRepository subjectRepository;

    // 자발적 예비 상담 예약
    public void preReserve (Long StudentId, PreReserveDto preReserveDto) {

        CounselingPreReserve counselingPreReserve = new CounselingPreReserve();
        Student student = studentRepository.findById(StudentId).orElseThrow(
                () -> new CustomRestfullException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        );
        Subject subject = subjectRepository.findById(preReserveDto.getSubjectId()).orElseThrow(
                () -> new CustomRestfullException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        );

        counselingPreReserve.setStudent(student);
        // 교수가 정한 예약 일정 저장
        counselingPreReserve.setCounselingSchedule(counselingScheduleRepository.findById(preReserveDto.getCounselingScheduleId()).orElseThrow());
        counselingPreReserve.setSubject(subject);
        counselingPreReserve.setReason(preReserveDto.getReason());

        // 해당 과목의 위험 학생인지 아닌지 조회
        StuSub stuSub = stuSubRepository.findByStudent_IdAndSubject_Id(StudentId, preReserveDto.getSubjectId()).orElseThrow(
                () -> new CustomRestfullException("성적을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        );

        if(dropoutRiskRepository.findByStuSubId(stuSub.getId()).isPresent()) {
            DropoutRisk dropoutRisk = dropoutRiskRepository.findByStuSubId(stuSub.getId()).orElseThrow(
                    () -> new CustomRestfullException("위험 학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            );
            counselingPreReserve.setDropoutRisk(dropoutRisk);
        }
        counselingPreReserveRepository.save(counselingPreReserve);
    }
    // 학생 - 상담 신청 내역 조회
    public List<CounselingPreReserveDto> loadReservations(Long studentId) {

        return counselingPreReserveRepository.findByStudentId(studentId)
                .stream()
                .map(CounselingPreReserveDto::new)
                .toList();
    }

    // 교수 - 상담 신청 내역 조회
    public List<CounselingPreReserveDto> loadPreList(Long professorId, Long subjectId) {

        // 조회할 subjectId 목록
        List<Long> subjectIds;

        if (subjectId != null) {
            subjectIds = List.of(subjectId);
        } else {
            subjectIds = subjectRepository.findByProfessor_Id(professorId)
                    .stream()
                    .map(Subject::getId)
                    .toList();
        }

        return counselingPreReserveRepository
                .findBySubject_IdIn(subjectIds)
                .stream()
                .map(CounselingPreReserveDto::new)
                .toList();
    }
}
