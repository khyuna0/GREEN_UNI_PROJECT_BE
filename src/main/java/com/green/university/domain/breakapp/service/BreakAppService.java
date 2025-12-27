package com.green.university.domain.breakapp.service;

import com.green.university.domain.breakapp.dto.BreakAppFormDto;
import com.green.university.domain.breakapp.dto.BreakUpdateDto;
import com.green.university.domain.breakapp.entity.BreakApp;
import com.green.university.domain.breakapp.repository.BreakAppRepository;
import com.green.university.domain.student.entity.Student;
import com.green.university.domain.student.repository.StudentRepository;
import com.green.university.domain.student.service.StuStatService;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.utils.TermUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class BreakAppService {

    private final BreakAppRepository breakAppRepository;
    private final StuStatService stuStatService;
    private final StudentRepository studentRepository;

    // 휴학 신청
    @Transactional
    public void createBreakApp(BreakAppFormDto dto) {
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
        breakApp.setStudent(student);
        breakApp.setStudentGrade(dto.getStudentGrade());
        breakApp.setFromYear(dto.getFromYear());
        breakApp.setFromSemester(dto.getFromSemester());
        breakApp.setToYear(dto.getToYear());
        breakApp.setToSemester(dto.getToSemester());
        breakApp.setType(dto.getType());
        breakApp.setAppDate(LocalDate.now());   // 신청일 오늘
        breakApp.setStatus("처리중");

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

    // 처리되지 않은 휴학 신청 수정
    @Transactional
    public void updateBreakApp(Long id, Long studentId, BreakUpdateDto dto){

        BreakApp breakApp = breakAppRepository.findById(id)
                .orElseThrow(() -> new CustomRestfullException("휴학 신청을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        
        // 처리중인 휴학신청만 수정가능
        if (!"처리중".equals(breakApp.getStatus())) {
            throw new CustomRestfullException("처리중인 신청만 수정할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }

        Long ownerId = breakApp.getStudent().getId();
        if (!ownerId.equals(studentId)) {
            throw new CustomRestfullException("본인 신청서만 수정할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        // ---- 비교키 만들기: (년도,학기) => 하나의 숫자로 비교 ----
        long nowKey  = TermUtil.currentYear() * 10 + TermUtil.currentSemester();   // 예: 20252
        long fromKey = breakApp.getFromYear() * 10 + breakApp.getFromSemester();  // 예: 20252
        long toKey   = dto.getToYear() * 10 + dto.getToSemester();               // 예: 20261

        // 종료가 '현재 학기' 이하이면 불가
        if (toKey <= nowKey) {
            throw new CustomRestfullException("종료 학기는 현재 학기 이후로만 설정할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }

        // 종료가 '시작 학기'보다 이전이면 불가
        // (현재보다 이후로 강제하면 보통 자동으로 걸리지만, 정책상 명시하는 게 좋음)
//        if (toKey < fromKey) {
//            throw new CustomRestfullException("종료 학기는 시작 학기보다 빠를 수 없습니다.", HttpStatus.BAD_REQUEST);
//        }

        breakApp.setToYear(dto.getToYear());
        breakApp.setToSemester(dto.getToSemester());
        breakApp.setType(dto.getType());

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




