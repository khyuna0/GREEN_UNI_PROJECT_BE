package com.green.university.controller;

import com.green.university.config.security.CustomUserDetails;
import com.green.university.dto.CounselingInfoDto;
import com.green.university.dto.response.PreReserveDto;
import com.green.university.service.CounselingScheduleService;
import com.green.university.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/preReserve")
public class CounselingPreReserveController {

    @Autowired
    private CounselingScheduleService counselingScheduleService;

    @Autowired
    private SubjectService subjectService;
    
    // 현 학기 수강 과목 기준 - 상담할 교수 검색
    @GetMapping("/byProfessorId")
    public ResponseEntity<?> getReserveLoad (
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) Long subId,
            @RequestParam LocalDate weekStartDate
    ) {
        Long id = principal.getId(); // 사용자 아이디 조회
        LocalDate weekEndDate = weekStartDate.plusDays(4); // 월~금

        List<CounselingInfoDto> counselingScheduleList = counselingScheduleService.getSchedulesByWeekAndSubId(subId, weekStartDate, weekEndDate);
        String subName = subjectService.readBySubjectId(subId).getName();

        return ResponseEntity.ok(Map.of(
                "counselingScheduleList", counselingScheduleList,
                "subName", subName
        ));
    }
    
    // 예비 상담 신청
    @PostMapping("/byProfessorId")
    public ResponseEntity<?> preReserve (
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody PreReserveDto preReserveDto
            ) {
        Long id = principal.getId(); // 사용자 아이디 조회


    }


}
