package com.green.university.domain.dropoutrisk.controller;

import com.green.university.domain.dropoutrisk.entity.RiskLevel;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.security.CustomUserDetails;
import com.green.university.domain.dropoutrisk.respository.DropoutRiskRepository;
import com.green.university.domain.dropoutrisk.dto.DropoutRiskResponseDto;
import com.green.university.domain.dropoutrisk.entity.DropoutRisk;
import com.green.university.domain.dropoutrisk.entity.RiskStatus;
import com.green.university.domain.dropoutrisk.service.DropoutRiskService;
import com.green.university.infra.ai.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/risk")
@PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR')")
public class DropoutRiskController {

    private final DropoutRiskRepository dropoutRiskRepository;
    private final AiAnalysisService aiAnalysisService;
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

    // 학생 내 위험 과목 리스트 조회
    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<DropoutRiskResponseDto>> getMyRisks(
            @AuthenticationPrincipal CustomUserDetails principal
            // status 파라미터 제거
            // DETECTED 때문에 상담요청 오면 리스트에서 사라짐
            // ,@RequestParam(required = false, defaultValue = "DETECTED") RiskStatus status
    ) {
        if (principal == null || !Objects.equals(principal.getUserRole(), "student")) {
            throw new CustomRestfullException("권한이 없는 페이지입니다.", HttpStatus.UNAUTHORIZED);
        }

        Long studentId = principal.getId();

        //  DETECTED + CONSULT_REQ 둘 다 조회해서
        // 교수 요청이 와도 위험과목 리스트에서 빠지지 않게 처리
        List<DropoutRisk> risks =
                dropoutRiskRepository.findByStuSub_Student_IdAndStatusIn(
                        studentId,
                        List.of(RiskStatus.DETECTED, RiskStatus.CONSULT_REQ)
                );

        return ResponseEntity.ok(risks.stream().map(DropoutRiskResponseDto::fromEntity).toList());
    }

    // =========================================================

    // (관리자/교수) 특정 riskId를 강제로 AI 재분석하고 싶을 때
    // - 이벤트 흐름 말고 "즉시 다시 돌리기" 버튼용
//    @PostMapping("/{riskId}/analyze")
//    public ResponseEntity<?> analyzeMerged(@PathVariable Long riskId) {
//        aiAnalysisService.analyzeAndSaveMerged(riskId); // void 메서드 호출
//        return ResponseEntity.ok(Map.of(
//                "riskId", riskId,
//                "message", "AI 분석 요청 완료"
//        ));
//    }
}
