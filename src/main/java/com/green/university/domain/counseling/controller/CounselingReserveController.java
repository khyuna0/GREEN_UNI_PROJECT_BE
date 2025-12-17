package com.green.university.domain.counseling.controller;

import com.green.university.domain.counseling.dto.CounselingProfessorRequestDto;
import com.green.university.domain.counseling.dto.CounselingStudentRequestDto;
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
            @Valid @RequestBody CounselingStudentRequestDto dto,
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

    // 처리되지 않은 학생 상담 신청 목록 조회
    @GetMapping("/notApplicated")
    public int getNotApplicated (
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long professorId = principal.getId();
        return counselingReserveService.getNotApproved(professorId);
    }

    // 학생 상담 개수 카운트용
    @GetMapping("/count/student")
    public java.util.Map<String, Integer> myCounts(@AuthenticationPrincipal CustomUserDetails principal) {
        Long studentId = principal.getId();
        return counselingReserveService.getMyCounts(studentId);
    }

    // 교수 -> 학생 상담요청
    @PostMapping("/pre/professor")
    public void professorRequest(
            @Valid @RequestBody CounselingProfessorRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long professorId = principal.getId();
        counselingReserveService.professorRequest(dto, professorId);
    }

    // 학생: 내가 받은 교수 상담요청 목록
    @GetMapping("/pre/list/student")
    public Object myPreList(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long studentId = principal.getId();
        return counselingReserveService.getMyPreReserveList(studentId);
    }

    // 학생: 수락 -> reserve 생성
    @PostMapping("/pre/accept")
    public Object acceptPre(
            @RequestParam Long preReserveId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long studentId = principal.getId();
        Long reserveId = counselingReserveService.acceptPreReserve(studentId, preReserveId);
        return java.util.Map.of("reserveId", reserveId);
    }

    // 학생: 거절
    @PostMapping("/pre/reject")
    public void rejectPre(
            @RequestParam Long preReserveId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long studentId = principal.getId();
        counselingReserveService.rejectPreReserve(studentId, preReserveId);
    }
}
