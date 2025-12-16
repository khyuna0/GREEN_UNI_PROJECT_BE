package com.green.university.domain.counseling.controller;

import com.green.university.domain.counseling.dto.CounselingInfoDto;
import com.green.university.domain.counseling.dto.CounselingPreReserveDto;
import com.green.university.domain.counseling.dto.PreReserveDto;
import com.green.university.domain.counseling.service.CounselingPreReserveService;
import com.green.university.domain.counseling.service.CounselingScheduleService;
import com.green.university.domain.subject.service.SubjectService;
import com.green.university.global.security.CustomUserDetails;
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
    private CounselingPreReserveService counselingPreReserveService;

    @Autowired
    private SubjectService subjectService;

    // 현 학기 수강 과목 기준 - 상담할 교수 검색
    @GetMapping("/byProfessorId")
    public ResponseEntity<?> getReserveLoad(
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

    // 예비 상담 신청 내역 조회 (학생)
    @GetMapping
    public ResponseEntity<?> getPreReserveList(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long id = principal.getId(); // 사용자 아이디 조회
        List<CounselingPreReserveDto> preList = counselingPreReserveService.loadReservations(id);

        return ResponseEntity.ok(Map.of(
                "preList", preList
        ));
    }


    // 예비 상담 신청
    @PostMapping
    public ResponseEntity<?> preReserve(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody PreReserveDto preReserveDto
    ) {
        Long id = principal.getId(); // 사용자 아이디 조회
        counselingPreReserveService.preReserve(id, preReserveDto);

        return ResponseEntity.ok().body("예비 상담 신청이 완료되었습니다!");
    }

    // 교수 - 이번 학기 강의 과목 기준 예비 상담 신청 내역 조회
    @GetMapping("/preList")
    public ResponseEntity<?> getPrelist(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) Long subjectId
    ) {
        Long id = principal.getId(); // 사용자 아이디
        List<CounselingPreReserveDto> preList = counselingPreReserveService.loadPreList(id, subjectId);

        return ResponseEntity.ok(Map.of(
                "preList", preList
        ));
    }


}
