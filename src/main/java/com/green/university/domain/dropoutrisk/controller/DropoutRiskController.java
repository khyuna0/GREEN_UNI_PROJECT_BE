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
    private final DropoutRiskService dropoutRiskService;

    // 해당 교수 + ai 위험 분석 결과를 상담 완료, 미완료로 테이블로 보여주기 + 검색 필터
    @GetMapping("/list/grouped")
    public ResponseEntity<?> getRisksByGroup(@RequestParam(required = false) Long subjectId,
                                             @RequestParam(required = false) String level,
                                             @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Long professorId = customUserDetails.getId();
        RiskLevel riskLevel = (level != null && !level.isEmpty()) ? RiskLevel.valueOf(level) : null;
        return ResponseEntity.ok(dropoutRiskService.getRisksByStatus(subjectId, riskLevel, professorId));
    }


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
