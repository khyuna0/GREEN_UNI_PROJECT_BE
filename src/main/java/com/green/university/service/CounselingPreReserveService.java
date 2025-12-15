package com.green.university.service;

import com.green.university.dto.response.PreReserveDto;
import com.green.university.entity.CounselingPreReserve;
import com.green.university.entity.Professor;
import com.green.university.entity.Student;
import com.green.university.exception.CustomRestfullException;
import com.green.university.repository.CounselingPreReserveRepository;
import com.green.university.repository.ProfessorRepository;
import com.green.university.repository.StuSubRepository;
import com.green.university.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CounselingPreReserveService {

    @Autowired
    private CounselingPreReserveRepository counselingPreReserveRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private StuSubRepository stuSubRepository;

    // 자발적 예비 상담 예약 (위험 타입 저장 안 함)
    private void preReserve (Long StudentId, PreReserveDto preReserveDto) {

        CounselingPreReserve counselingPreReserve = new CounselingPreReserve();
        Student student = studentRepository.findById(StudentId).orElseThrow(
                () -> new CustomRestfullException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        );
        Professor professor = professorRepository.findById(preReserveDto.getProfessorId()).orElseThrow(
                () -> new CustomRestfullException("교수를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
        );

        counselingPreReserve.setStudent(student);
        counselingPreReserve.setProfessor(professor);
        counselingPreReserve.setCounselingSchedule(preReserveDto.getCounselingSchedule());
        counselingPreReserve.setReason(preReserveDto.getReason());

        counselingPreReserveRepository.save(counselingPreReserve);
    }
}
