package com.green.university.domain.dropoutrisk.controller;

import com.green.university.domain.dropoutrisk.dto.DropoutRiskResponseDto;
import com.green.university.domain.dropoutrisk.entity.DropoutRisk;
import com.green.university.domain.dropoutrisk.entity.RiskLevel;
import com.green.university.domain.dropoutrisk.entity.RiskStatus;
import com.green.university.domain.dropoutrisk.respository.DropoutRiskRepository;
import com.green.university.domain.dropoutrisk.service.DropoutRiskService;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.security.CustomUserDetails;
import com.green.university.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/risk")
@PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR')")
public class DropoutRiskController {

    private final DropoutRiskRepository dropoutRiskRepository;
    private final DropoutRiskService dropoutRiskService;

    // (조회) 해당 교수 + ai 위험 분석 결과를 상담 완료, 미완료로 테이블로 보여주기 + 검색 필터
    @GetMapping("/list/grouped")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<?> getRisksByGroup(@RequestParam(required = false) Long subjectId,
                                             @RequestParam(required = false) String level,
                                             @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        Long professorId = customUserDetails.getId();
        RiskLevel riskLevel = (level != null && !level.isEmpty()) ? RiskLevel.valueOf(level) : null;

        // 과목 담당교수 기준
        var grouped = dropoutRiskService.getRisksByStatus(subjectId, riskLevel, professorId);

        // 학과 기준 통합(탈락 위험)
        // subjectId는 통합 학생 리스트에선 보통 의미 없어서(학과 전체) 무시 추천
        var students = dropoutRiskService.getStudentOverallRisks(null, riskLevel, professorId);

        return ResponseEntity.ok(java.util.Map.of(
                "pending", grouped.get("pending"),
                "resolved", grouped.get("resolved"),
                "students", students
        ));
    }

    // 교수가 본인의 특정 과목(subjectId)에서 위험 학생 목록 조회
    @GetMapping("/{subjectId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<List<DropoutRiskResponseDto>> getRiskStudents(
            @PathVariable Long subjectId,
            @RequestParam(required = false, defaultValue = "DETECTED") RiskStatus status) {

        // Repository에 쿼리 메서드 필요: findByStuSub_Subject_IdAndStatus(Long subjectId, RiskStatus status)
        var risks = dropoutRiskRepository.findByStuSub_Subject_IdAndStatus(subjectId, status);

        List<DropoutRiskResponseDto> dtos = risks.stream()
                .map(DropoutRiskResponseDto::fromEntity) // DTO 변환 메서드 사용
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // 우리학과 중도이탈 위험 학생: 선택 학생의 위험과목 전체 조회
    @GetMapping("/list/department")
    @PreAuthorize("hasRole('PROFESSOR')")
    public Map<String, Object> departmentPending(
            @RequestParam Long studentId,
            @RequestParam(required = false) RiskLevel level,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long professorId = principal.getId();
        List<DropoutRiskResponseDto> pending =
                dropoutRiskService.getDepartmentPendingRisks(studentId, level, professorId);
        return Map.of("pending", pending);
    }

    // 학생 내 위험 과목 리스트 조회
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<DropoutRiskResponseDto>> getMyRisks(
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long studentId = principal.getId();
        List<DropoutRisk> risks = dropoutRiskRepository.findByStuSub_Student_Id(studentId);
        return ResponseEntity.ok(risks.stream().map(DropoutRiskResponseDto::fromEntity).toList());
    }


    // 교수가 상담 종료를 눌렀을 때 /videotest?code=1234 로 온 값 받아서 status를 resolved/finished로 바꾸기
    @GetMapping("/counseling/done")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<?> counselingDone(@RequestParam String roomCode,
                                            @AuthenticationPrincipal CustomUserDetails principal) {
        dropoutRiskService.completeCounseling(roomCode);
        return ResponseEntity.ok("상담 완료");
    }

}
