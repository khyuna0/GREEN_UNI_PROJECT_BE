package com.green.university.infra.ai.controller;

import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.security.CustomUserDetails;
import com.green.university.infra.ai.DropoutRiskRepository;
import com.green.university.infra.ai.dto.response.DropoutRiskResponseDto;
import com.green.university.infra.ai.dto.response.DropoutRiskRowDto;
import com.green.university.infra.ai.entity.DropoutRisk;
import com.green.university.infra.ai.entity.RiskStatus;
import com.green.university.infra.ai.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/risk")
public class DropoutRiskController {

    private final DropoutRiskRepository dropoutRiskRepository;
    private final AiAnalysisService aiAnalysisService;

    // 교수가 본인의 특정 과목(subjectId)에서 위험 학생 목록 조회
    @GetMapping("/{subjectId}")
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
    public ResponseEntity<List<DropoutRiskRowDto>> getMyRisks(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false, defaultValue = "DETECTED") RiskStatus status
    ) {
        if (principal == null || !Objects.equals(principal.getUserRole(), "student")) {
            throw new CustomRestfullException("권한이 없는 페이지입니다.", HttpStatus.UNAUTHORIZED);
        }

        Long studentId = principal.getId();

        List<DropoutRisk> risks =
                dropoutRiskRepository.findByStuSub_Student_IdAndStatus(studentId, status);

        return ResponseEntity.ok(risks.stream().map(DropoutRiskRowDto::from).toList());
    }


//    // Gemini로 위험 학생 분석
//    @PostMapping("/{riskId}/analyze/gemini")
//    public Mono<RiskNotificationDto> analyzeWithGemini(@PathVariable Long riskId) {
//        return aiAnalysisService.analyzeAndSaveWithGemini(riskId);
//    }
//
//    // Mistral로 위험 학생 분석
//    @PostMapping("/{riskId}/analyze/mistral")
//    public Mono<RiskNotificationDto> analyzeWithMistral(@PathVariable Long riskId) {
//        return aiAnalysisService.analyzeAndSaveWithMistral(riskId);
//    }

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

//     // (교수용) 내 강의의 위험 학생 리스트 조회
//    @GetMapping("/professor/list")
//    public ResponseEntity<?> getRiskStudentsForProfessor(@AuthenticationPrincipal CustomUserDetails principal) {
//        // principal.getId()가 교수 ID라고 가정
//        List<DropoutRisk> risks = dropoutRiskRepository.findBySubject_Professor_Id(principal.getId());
//        return ResponseEntity.ok(risks);
//    }
//
//    // (학생용) 나의 경고/위험 알림 조회 (홈 화면 배너용)
//    @GetMapping("/my-status")
//    public ResponseEntity<?> getMyRiskStatus(@AuthenticationPrincipal CustomUserDetails principal) {
//        // 해결된(RESOLVED) 건은 제외하고 조회
//        List<DropoutRisk> myRisks = dropoutRiskRepository.findByStudent_IdAndStatusNot(principal.getId(), RiskStatus.RESOLVED);
//        return ResponseEntity.ok(myRisks);
//    }
//
//    // (공통) 위험 상세 조회 (AI 분석 내용 포함)
//    @GetMapping("/{riskId}")
//    public ResponseEntity<?> getRiskDetail(@PathVariable Long riskId) {
//        DropoutRisk risk = dropoutRiskRepository.findById(riskId)
//                .orElseThrow(() -> new RuntimeException("해당 내역이 없습니다."));
//        return ResponseEntity.ok(risk);
//    }
}