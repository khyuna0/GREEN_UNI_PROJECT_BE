package com.green.university.domain.dropoutrisk.service;

import com.green.university.domain.dropoutrisk.dto.DropoutRiskRowDto;
import com.green.university.domain.dropoutrisk.respository.DropoutRiskRepository;
import com.green.university.domain.grade.service.GradeService;
import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.entity.StuSubDetail;
import com.green.university.infra.ai.dto.AiRiskAnalysisRequest;
import com.green.university.infra.ai.dto.response.AiRiskAnalysisResult;
import com.green.university.domain.dropoutrisk.entity.DropoutRisk;
import com.green.university.domain.dropoutrisk.entity.RiskLevel;
import com.green.university.domain.dropoutrisk.entity.RiskStatus;
import com.green.university.domain.dropoutrisk.entity.RiskType;
import com.green.university.infra.ai.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
// 출석 및 성적 기반 위험도 계산 + AI 분석 요청 + DB 저장
public class DropoutRiskService {

    private final DropoutRiskRepository dropoutRiskRepository;
    private final AiAnalysisService aiAnalysisService; // AI API 호출 서비스
    private final GradeService gradeService;

    // ================= 조회 로직 추가 =================
    @Transactional(readOnly = true)
    public List<DropoutRiskRowDto> getRisksBySubject(Long subjectId) {
        return dropoutRiskRepository.findByStuSub_Subject_Id(subjectId)
                .stream()
                .map(DropoutRiskRowDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DropoutRiskRowDto> getAllRisks() {
        return dropoutRiskRepository.findAll()
                .stream()
                .map(DropoutRiskRowDto::from)
                .toList();
    }

    // =============== 기존 평가+저장 로직 ===============
    // 성적 및 출결 변경 시 호출되는 메인 메서드
    @Transactional
    public void evaluateAndAnalyzeRisk(StuSub stuSub, StuSubDetail detail) {

        // 1. 위험도 계산 (비즈니스 로직)
        RiskAnalysis analysis = calculateRisk(detail);

        // 위험이 없으면(NULL) 기존 리스크가 있다면 해결(RESOLVED) 처리 후 종료하거나, 그냥 종료
        if (analysis == null) {
            // 필요하다면 여기서 기존 DETECTED 상태인 리스크를 찾아서 RESOLVED로 바꾸는 로직 추가 가능
            return;
        }

        // 2. AI 분석 요청 데이터 생성
        // 직전 학기 평점 가져오기 (없으면 0.0)
        Double prevGpa = 0.0;
        try {
            // GradeService의 로직에 따라 약간 다를 수 있음 (Null 체크 필수)
            var myGrade = gradeService.readMyGradeByStudentId(stuSub.getStudent().getId());
            if (myGrade != null) prevGpa = (double) myGrade.getAverage();
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

        // 3. AI 서비스 호출 (오래 걸릴 수 있으므로 비동기로 빼는게 좋지만, 일단 동기로 진행)
        AiRiskAnalysisResult aiResult = aiAnalysisService.analyzeRisk(request);

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
        log.info("학생({}) 위험 분석 저장 완료: {}", stuSub.getStudent().getName(), analysis.type);
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

