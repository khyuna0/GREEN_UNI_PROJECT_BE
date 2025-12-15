package com.green.university.controller;

import com.green.university.config.security.CustomUserDetails;
import com.green.university.dto.WeeklyCounselingScheduleRequest;
import com.green.university.entity.CounselingSchedule;
import com.green.university.exception.CustomRestfullException;
import com.green.university.service.CounselingScheduleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/counseling")
public class CounselingController {

    @Autowired
    private CounselingScheduleService counselingScheduleService;

    //

    @GetMapping("/professor") // 교수 - 내 상담 일정 불러오기
    public ResponseEntity<?> getSchedule (@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null || !Objects.equals(principal.getUserRole(), "professor")) {
            throw new CustomRestfullException("권한이 없는 페이지입니다.", HttpStatus.UNAUTHORIZED);
        }

        Long id = principal.getId();
        List<CounselingSchedule> list = counselingScheduleService.getSchedules(id);

        return  ResponseEntity.ok().body(list);
    }

    @PostMapping("/professor") // 교수 - 내 상담 일정 등록
    public ResponseEntity<?> WeeklyCounselingSchedule (@AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody WeeklyCounselingScheduleRequest weeklyDto) {
        if (principal == null || !Objects.equals(principal.getUserRole(), "professor")) {
            throw new CustomRestfullException("권한이 없는 페이지입니다.", HttpStatus.UNAUTHORIZED);
        }

        Long professorId = principal.getId(); // 로그인 교수
        counselingScheduleService.createWeeklySchedule(professorId, weeklyDto);

        return ResponseEntity.ok().body("일정 등록이 완료되었습니다");
    }


}
