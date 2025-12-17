package com.green.university.domain.counseling.controller;

import com.green.university.domain.counseling.dto.CounselingReserveRequestDto;
import com.green.university.domain.counseling.service.CounselingReserveService;
import com.green.university.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reserve")
public class CounselingReserveController {

    private final CounselingReserveService counselingReserveService;

    public CounselingReserveController(CounselingReserveService counselingReserveService) {
        this.counselingReserveService = counselingReserveService;
    }

    // 학생 상담 신청
    @PostMapping
    public void requestReserve(
            @Valid @RequestBody CounselingReserveRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        // 로그인한 사용자 ID 추출
        Long studentId = principal.getId();

        counselingReserveService.requestReserve(dto, studentId);
    }

    // 교수 승인 / 반려
    @PostMapping("/decision")
    public void decideReserve(
            @RequestParam Long reserveId,
            @RequestParam String decision,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        // 교수 권한 체크는 SecurityConfig 또는 AOP에서 처리
        counselingReserveService.decideReserve(reserveId, decision);
    }

    // 학생 상담 예약 목록
    @GetMapping("/list")
    public Object studentList(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long studentId = principal.getId();
        return counselingReserveService.getStudentReservationList(studentId);
    }

    // 교수 상담 예약 목록
    @GetMapping("/list/professor")
    public Object professorList(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long professorId = principal.getId();
        return counselingReserveService.getProfessorReservationList(professorId);
    }

    // 포탈 알림 용 - 교수

    // 오늘의 상담 일정 보기
//    @GetMapping("/alert/today")
//    public int


    // 미승인/반려 된 상담 요청 개수 보기

}
