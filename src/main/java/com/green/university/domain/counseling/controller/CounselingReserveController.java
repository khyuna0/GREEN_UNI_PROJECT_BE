package com.green.university.domain.counseling.controller;

import com.green.university.domain.counseling.dto.CounselingPreReserveFormDto;
import com.green.university.domain.counseling.dto.CounselingReserveDto;
import com.green.university.domain.counseling.service.CounselingReserveService;
import com.green.university.global.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reserve")
public class CounselingReserveController {

    @Autowired
    CounselingReserveService counselingReserveService;

    // 가예약 확정 후 요청 받은 같은 시간 예약들 반려로 돌리고, 진짜 예약 생성 + 룸키까지 생성
    // 반려면 업데이트만 하고 리턴
    @PostMapping
    public ResponseEntity<?> reserve(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody CounselingPreReserveFormDto counselingPreReserveFormDto
            ) {
        if(counselingPreReserveFormDto.getDecision().equals("반려")) { // 반려일 때
            counselingReserveService.reject(counselingPreReserveFormDto);
        } else { // 승인일 때
            counselingReserveService.confirmReservation
                    (counselingPreReserveFormDto);
        }
        return ResponseEntity.ok().body("예약 승인 또는 반려가 완료되었습니다.");
    }

    // 학생 - 내 상담 일정 보기
    @GetMapping("/list")
    public ResponseEntity<?> getReservationList (@AuthenticationPrincipal CustomUserDetails principal) {

        Long studentId = principal.getId();
        List<CounselingReserveDto> reservationList = counselingReserveService.getReservationList(studentId);

        return ResponseEntity.ok(Map.of(
                "reservationList", reservationList
        ));
    }
}
