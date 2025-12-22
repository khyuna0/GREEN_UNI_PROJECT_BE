package com.green.university.domain.dropoutrisk.service;

import com.green.university.domain.counseling.entity.ApprovalState;
import com.green.university.domain.counseling.entity.CounselingReserve;
import com.green.university.domain.counseling.entity.ReserveRequester;
import com.green.university.domain.counseling.repository.CounselingReserveRepository; // ✅ [추가]
import com.green.university.domain.dropoutrisk.dto.DropoutRiskResponseDto;
import com.green.university.domain.dropoutrisk.respository.DropoutRiskRepository;
import com.green.university.domain.grade.service.GradeService;
import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.entity.StuSubDetail;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.domain.subject.repository.SubjectRepository;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.infra.ai.dto.AiRiskAnalysisRequest;
import com.green.university.infra.ai.dto.AiRiskAnalysisResult;
import com.green.university.domain.dropoutrisk.entity.DropoutRisk;
import com.green.university.domain.dropoutrisk.entity.RiskLevel;
import com.green.university.domain.dropoutrisk.entity.RiskStatus;
import com.green.university.domain.dropoutrisk.entity.RiskType;
import com.green.university.infra.ai.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
// ☎️ 출결 및 성적 기반 위험도 계산 + AI 분석 요청 + DB 저장 + 조회 (위험 학생 목록 가져오기)
public class DropoutRiskService {

    private final DropoutRiskRepository dropoutRiskRepository;
    private final AiAnalysisService aiAnalysisService; // AI API 호출 서비스
    private final GradeService gradeService;
    private final SubjectRepository subjectRepository;

    private final CounselingReserveRepository counselingReserveRepository; // consultState 계산용

    // ================= 조회 로직 추가 =================
    // 해당 교수의 강의 중 + 상담 미완료, 완료된 학생을 나눠서 보여주기 + 검색 (과목, 위험레벨)
    @Transactional(readOnly = true)
    public Map<String, List<DropoutRiskResponseDto>> getRisksByStatus(Long subjectId, RiskLevel riskLevel, Long professorId) {
        List<DropoutRisk> dropoutRisks;

        // 1. subjectId 있으면 해당 과목이 교수 강의인지 검증
        if (subjectId != null) {
            Subject subject = subjectRepository.findById(subjectId).orElseThrow(
                    () -> new CustomRestfullException("해당 과목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            );
            if (!subject.getProfessor().getId().equals(professorId)) {
                throw new CustomRestfullException("본인 강의만 조회 할 수 있습니다.", HttpStatus.FORBIDDEN);
            }
        }

        // 2. 조건에 따라 조회 (항상 professorId 포함)
        if (subjectId == null && riskLevel == null) {
            // 해당 교수의 모든 과목 위험학생
            dropoutRisks = dropoutRiskRepository.findByStuSub_Subject_Professor_Id(professorId);
        } else if (riskLevel == null) {
            // 특정 과목 위험학생 (이미 위에서 교수 검증 완료)
            dropoutRisks = dropoutRiskRepository.findByStuSub_Subject_Id(subjectId);
        } else if (subjectId == null) {
            // 해당 교수의 모든 과목 중 특정 위험레벨
            dropoutRisks = dropoutRiskRepository.findByStuSub_Subject_Professor_IdAndRiskLevel(professorId, riskLevel);
        } else {
            // 특정 과목 + 특정 위험레벨 (이미 위에서 교수 검증 완료)
            dropoutRisks = dropoutRiskRepository.findByStuSub_Subject_IdAndRiskLevel(subjectId, riskLevel);
        }

        Map<String, List<DropoutRiskResponseDto>> map = new HashMap<>();

        map.put("pending", dropoutRisks.stream().filter(
                        r -> r.getStatus().equals(RiskStatus.DETECTED) || r.getStatus().equals(RiskStatus.CONSULT_REQ))
                .map(r -> {
                    // 교수요청 최신 상태를 보고 "거절됨(재요청 가능)" 계산
                    String consultState = computeConsultState(r);
                    return DropoutRiskResponseDto.fromEntity(r, consultState);
                })
                .collect(Collectors.toList()));

        map.put("resolved", dropoutRisks.stream().filter(
                        r -> r.getStatus().equals(RiskStatus.RESOLVED))
                .map(r -> {
                    String consultState = computeConsultState(r); // resolved도 같이 내려주고 싶으면 유지
                    return DropoutRiskResponseDto.fromEntity(r, consultState);
                })
                .collect(Collectors.toList()));

        return map;
    }

    // 교수요청 최신 reserve 상태를 기반으로 UI 표시용 문자열 반환
    // - REQUESTED -> "CONSULT_REQ"
    // - REJECTED  -> "CONSULT_REJECTED"
    // - APPROVED  -> "CONSULT_APPROVED"
    // - 없으면 null
    private String computeConsultState(DropoutRisk risk) {
        if (risk == null || risk.getStuSub() == null) return null;

        Long studentId = risk.getStuSub().getStudent().getId();
        Long subjectId = risk.getStuSub().getSubject().getId();

        Optional<CounselingReserve> latestOpt = counselingReserveRepository
                .findTop1ByStudent_IdAndSubject_IdAndRequesterOrderByIdDesc(
                        studentId,
                        subjectId,
                        ReserveRequester.PROFESSOR
                );

        if (latestOpt.isEmpty()) return null;
        CounselingReserve latest = latestOpt.get();

        if (latest.getApprovalState() == null) return null;

        if (latest.getApprovalState() == ApprovalState.REQUESTED) return "CONSULT_REQ";
        if (latest.getApprovalState() == ApprovalState.REJECTED)  return "CONSULT_REJECTED";
        if (latest.getApprovalState() == ApprovalState.APPROVED)  return "CONSULT_APPROVED";
        if (latest.getApprovalState() == ApprovalState.CANCELED)  return "CONSULT_CANCELED"; // 상담 취소됨 표시 - 재신청 가능하게

        return null;
    }

    // =============== AI 분석 + 저장 로직 ===============
    // 출결 및 성적 기반 위험도 계산 + AI 분석 요청 + DB 저장
    @Transactional
    public void evaluateAndAnalyzeRisk(StuSub stuSub, StuSubDetail detail) {
        log.info("🔍 AI 분석 시작 - 학생: {}, 과목: {}",
                stuSub.getStudent().getName(),
                stuSub.getSubject().getName());

        // 1. 위험도 계산 (비즈니스 로직)
        RiskAnalysis analysis = calculateRisk(detail);

        // 위험이 없으면(NULL) 기존 리스크가 있다면 해결(RESOLVED) 처리 후 종료하거나, 그냥 종료
        if (analysis == null) {
            log.info("⚠ 위험 없음 - 분석 종료 (학생: {})", stuSub.getStudent().getName());
            // 필요하다면 여기서 기존 DETECTED 상태인 리스크를 찾아서 RESOLVED로 바꾸는 로직 추가 가능
            return;
        }

        log.info("📊 위험 감지됨 - Type: {}, Level: {}", analysis.type, analysis.level);

        // 2. AI 분석 요청 데이터 생성
        // 직전 학기 평점 가져오기 (없으면 0.0)
        double prevGpa = 0.0;
        try {
            // GradeService readMyGradeByStudentId는 금학기 누계 성적임..
            var myGrade = gradeService.readMyGradeByStudentId(stuSub.getStudent().getId());
            if (myGrade != null) prevGpa = myGrade.getAverage();
        } catch (Exception e) {
            log.warn("GPA 조회 실패 (신입생 등): {}", e.getMessage());
        }

        AiRiskAnalysisRequest request = AiRiskAnalysisRequest.builder()
                .studentId(stuSub.getStudent().getId())
                .studentName(stuSub.getStudent().getName())
                .subjectId(stuSub.getSubject().getId())
                .subjectName(stuSub.getSubject().getName())
                .absent(detail.getAbsent())
                .lateness(detail.getLateness())
                .convertedMark(detail.getConvertedMark())
                .letterGrade(detail.getLetterGrade())
                .semesterGpa(prevGpa)
                .riskType(analysis.type)
                .riskLevel(analysis.level)
                .build();

        // 3. AI 서비스 호출 (오래 걸릴 수 있음 - 동기 + 블로킹 방식)
        log.info("🤖 AI 서비스 호출 중...");
        AiRiskAnalysisResult aiResult = aiAnalysisService.analyzeRisk(request);
        log.info("✅ AI 응답 받음: {}", aiResult.getSummary());

        // 4. DB 저장 (Update or Insert)
        DropoutRisk risk = dropoutRiskRepository.findByStuSubAndRiskType(stuSub, analysis.type)
                .orElseGet(() -> DropoutRisk.builder()
                        .stuSub(stuSub)
                        .riskType(analysis.type)
                        .build());

        // 내용 업데이트
        risk.setRiskLevel(analysis.level);
        risk.setStatus(RiskStatus.DETECTED); // 다시 위험 감지됨
        risk.setLastAiInput(request.toString()); // 혹은 핵심 요약만

        // AI 결과 매핑
        risk.setAiSummary(aiResult.getSummary());
        risk.setAiRecommendation(aiResult.getProfessorGuide());
        risk.setAiStudentMessage(aiResult.getStudentMessage());
        // 태그 리스트는 String으로 변환해서 저장 (예: "결석,성적")
        if (aiResult.getReasonTags() != null) {
            risk.setAiReasonTags(String.join(",", aiResult.getReasonTags()));
        }

        dropoutRiskRepository.save(risk);
        log.info("💾 DB 저장 완료 - riskId: {}, studentName: {}, type: {}", risk.getId(), stuSub.getStudent().getName(), analysis.type);
    }

    // 내부적으로 쓰는 위험 분석 결과 클래스
    private record RiskAnalysis(RiskType type, RiskLevel level) {
    }

    // 위험도 계산 로직 분리
    private RiskAnalysis calculateRisk(StuSubDetail detail) {
        long absent = detail.getAbsent() == null ? 0 : detail.getAbsent();
        long lateness = detail.getLateness() == null ? 0 : detail.getLateness();
        long totalAbsent = absent + (lateness / 3);
        // 지각 3회 = 결석 1회로 치환한 총 결석 수
        String grade = detail.getLetterGrade();
        boolean attendanceDanger = totalAbsent >= 4;
        boolean gradeDanger = "F".equalsIgnoreCase(grade);

        // 1. 위험 레벨: DANGER (결석 4회 이상 OR F학점)
        // DANGER
        if (attendanceDanger && gradeDanger) {
            return new RiskAnalysis(RiskType.BOTH, RiskLevel.DANGER);
        }
        if (attendanceDanger) {
            return new RiskAnalysis(RiskType.ATTENDANCE, RiskLevel.DANGER);
        }
        if (gradeDanger) {
            return new RiskAnalysis(RiskType.SUBJECT_GRADE, RiskLevel.DANGER);
        }

        // 2. 위험 레벨: WARNING (결석 3회 이상 OR 점수가 낮음 등)
        if (totalAbsent >= 3) {
            return new RiskAnalysis(RiskType.ATTENDANCE, RiskLevel.WARNING);
        }

        // 성적 경고 기준 (예: C+ 이하이거나 환산점수 70점 미만 - 규칙은 정하기 나름)
        if (detail.getConvertedMark() != null && detail.getConvertedMark() < 70.0) {
            return new RiskAnalysis(RiskType.SUBJECT_GRADE, RiskLevel.WARNING);
        }

        return null; // 정상
    }
}
