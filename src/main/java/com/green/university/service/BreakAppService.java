package com.green.university.service;

import com.green.university.dto.BreakAppFormDto;
import com.green.university.entity.Student;
import com.green.university.exception.CustomRestfullException;
import com.green.university.repository.BreakAppRepository;
import com.green.university.entity.BreakApp;
import com.green.university.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * @author 서영
 *
 */

@Service
public class BreakAppService {

    @Autowired
    private BreakAppRepository breakAppRepository;

    @Autowired
    private StuStatService stuStatService;

    @Autowired
    private StudentRepository studentRepository;

    // 휴학 신청
    @Transactional
    public void createBreakApp(BreakAppFormDto dto) {

        // 학생 엔티티 조회 (기존: studentId Long만 다님 → JPA는 객체로!)
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new CustomRestfullException("학생을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 이미 처리중인 휴학 신청이 있는지 검사
        List<BreakApp> breakAppList = breakAppRepository.findByStudent_IdOrderByIdDesc(dto.getStudentId());
        for (BreakApp b : breakAppList) {
            if ("처리중".equals(b.getStatus())) {
                throw new CustomRestfullException("이미 처리중인 신청 내역이 존재합니다.", HttpStatus.CONFLICT);
            }
        }

        // 엔티티 값 세팅
        BreakApp breakApp = new BreakApp();
        breakApp.setStudent(student);                         // 외래키 대신 엔티티
        breakApp.setStudentGrade(dto.getStudentGrade());
        breakApp.setFromYear(dto.getFromYear());
        breakApp.setFromSemester(dto.getFromSemester());
        breakApp.setToYear(dto.getToYear());
        breakApp.setToSemester(dto.getToSemester());
        breakApp.setType(dto.getType());
        breakApp.setAppDate(LocalDate.now());   // 신청일 오늘
        breakApp.setStatus("처리중");

        // 저장
        breakAppRepository.save(breakApp);
    }

    // 해당 학생의 휴학 신청 내역 조회
    @Transactional(readOnly = true)
    public List<BreakApp> readByStudentId(Long studentId) {
        return breakAppRepository.findByStudent_IdOrderByIdDesc(studentId);
    }

    // 처리되지 않은 휴학 신청 내역 조회 (교직원)
    @Transactional(readOnly = true)
    public List<BreakApp> readByStatus(String status) {
        return breakAppRepository.findByStatus(status);
    }

    // 특정 휴학 신청서 조회
    @Transactional(readOnly = true)
    public BreakApp readById(Long id) {
        return breakAppRepository.findById(id)
                .orElseThrow(() ->
                        new CustomRestfullException("휴학 신청을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    // 처리되지 않은 휴학 신청 취소, 삭제
    @Transactional
    public void deleteById(Long id) {
        BreakApp breakApp = readById(id);
        if (!"처리중".equals(breakApp.getStatus())) {
            throw new CustomRestfullException("이미 처리가 완료되어, 신청이 취소되지 않았습니다.",
                    HttpStatus.BAD_REQUEST);
        }
        breakAppRepository.delete(breakApp);
    }

    // 휴학 신청 처리 (교직원)
    @Transactional
    public void updateById(Long id, String status) {

        BreakApp breakApp = readById(id);
        breakApp.setStatus(status);  // 변경 감지(dirty checking)로 update 수행

        // 승인 시 학적 상태를 휴학으로 변경
        if ("승인".equals(status)) {
            String newToDate;
            if (breakApp.getToSemester() == 1) {
                newToDate = breakApp.getToYear() + "-08-31";
            } else {
                newToDate = (breakApp.getToYear() + 1) + "-02-28";
            }

            Long studentId = breakApp.getStudent().getId();   // 엔티티에서 ID 꺼내기

            // 원래 StudentService.updateStatus(...) 라고 되어있던 걸
            // 실제 사용하는 Service 빈으로 변경해야 함. (예: stuStatService)
            stuStatService.updateStatus(studentId, "휴학", newToDate, id);
        }
    }
}




