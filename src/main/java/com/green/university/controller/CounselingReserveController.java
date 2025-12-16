package com.green.university.controller;

import com.green.university.config.security.CustomUserDetails;
import com.green.university.dto.CounselingReserveDto;
import com.green.university.service.CounselingReserveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reserve")
public class CounselingReserveController {

    @Autowired
    CounselingReserveService counselingReserveService;

    // 가예약 확정 후 요청 받은 같은 시간 예약들 반려로 돌리고, 진짜 예약 생성 + 룸키까지 생성
    // 반려면 그냥 업데이트만 하고 리턴
    @PostMapping
    public ResponseEntity<?> reserve(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody CounselingReserveDto counselingReserveDto
            ) {
        if(counselingReserveDto.getDecision().equals("반려")) { // 반려일 때
            counselingReserveService.reject(counselingReserveDto); 
        } else { // 승인일 때
            counselingReserveService.confirmReservation
                    (counselingReserveDto);
        }
        return ResponseEntity.ok().body("예약 승인 또는 반려가 완료되었습니다.");
    }
}
