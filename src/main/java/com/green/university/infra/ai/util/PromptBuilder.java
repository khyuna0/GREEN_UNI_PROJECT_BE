package com.green.university.infra.ai.util;

import com.green.university.domain.dropoutrisk.entity.RiskType;
import com.green.university.infra.ai.dto.AiRiskAnalysisRequest;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    // 학생 위험도 분석용 프롬프트 생성
    // 나중에 프롬프트 버전 관리할 때 여기만 수정
    public String buildRiskAnalysisPrompt(AiRiskAnalysisRequest req) {
        // ✅ 자바가 상황을 미리 정리해서 AI에게 전달 (AI가 딴소리 못하게 원천 차단)
        String analysisGuide = buildAnalysisContext(req);

        return """
                당신은 20년 이상 경력의 대학 학사 경고 전문 상담 컨설턴트입니다.
                제공된 [핵심 분석 가이드]를 기반으로 학생 상태를 진단하고
                분석해 중도 이탈(자퇴/제적) 위험을 진단하고 JSON 포맷으로 응답하세요.
                불필요한 서론이나 마크다운(```)은 삭제하세요.
                
                [학생 정보]
                - 이름: %s
                - 과목: %s
                - 결석/지각: %d회 / %d회 (지각 3회=결석 1회)
                - 상세점수: 과제 %d점 / 중간 %d점 / 기말 %d점 (각 100점 만점 기준)
                - 위험 유형: %s (ATTENDANCE=출석만문제, SUBJECT_GRADE=성적만문제, BOTH=총체적난국)
                - 핵심 분석 가이드: "%s"
                
                [JSON 필수 키]
                { "summary": "학생 상태에 대한 날카로운 1줄 핵심 요약 (30자 이내)",
                  "professorGuide": "교수가 학생과 상담할 때 던져야 할 구체적인 조언 (경어체 사용, 120자 이내)",
                  "studentMessage": "학생에게 시스템 알림으로 보낼 따뜻하지만 경각심을 주는 격려의 문장 (2문장 이내)",
                  "reasonTags": ["태그1", "태그2"]  // 최대 3개, 아래 태그 목록에서만 선택
                }
                
                [태그 선택 기준]
                - 결석 4회 이상 → "잦은결석"
                - 지각 6회 이상 → "지각누적"
                - 점수가 전반적으로 낮음(모두 60점 미만) → "기초학습부족"
                - 중간/기말 중 하나라도 90점 이상 → "성적우수"
                - BOTH → "중도이탈위험", "심층상담필요"
                - 출석은 좋은데 점수가 낮음 → "학습법코칭필요"
                
                [주의]
                summary, professorGuide, studentMessage 세 필드는 모두 반드시 문자열(String)로만 작성하세요.
                reasonTags는 반드시 배열 형태로, 위 목록에 없는 태그 사용 금지.
                객체나 배열 사용 금지.
                """.formatted(
                req.getStudentName(),
                req.getSubjectName(),
                req.getAbsent(),
                req.getLateness(),
                req.getHomework(),
                req.getMidExam(),
                req.getFinalExam(),
                req.getRiskType(),
                analysisGuide
        );
    }

    // 💡 RiskType과 점수를 보고 AI가 할 말을 정해주는 메서드
    private String buildAnalysisContext(AiRiskAnalysisRequest req) {
        // 과제 + 중간 + 기말 점수 평균
        double hw = req.getHomework() == null ? 0 : req.getHomework();
        double mid = req.getMidExam() == null ? 0 : req.getMidExam();
        double fin = req.getFinalExam() == null ? 0 : req.getFinalExam();
        double avgScore = (hw + mid + fin) / 3.0;

        // 1. 출석만 문제인 경우 (공부는 잘함)
        if (req.getRiskType() == RiskType.ATTENDANCE) {
            if (avgScore >= 80) {
                return "시험 성적은 매우 우수하나 잦은 결석으로 F 학점 위기입니다. 성실성 결여가 주된 원인입니다.";
            } else {
                return "성적은 준수하나 결석이 잦아 이수 기준 미달 위험이 있습니다.";
            }
        }

        // 2. 성적만 문제인 경우 (학교는 잘 옴)
        if (req.getRiskType() == RiskType.SUBJECT_GRADE) {
            return "출석은 양호하나 전반적인 학업 성취도가 낮아 학습 동기 부여나 기초 학습 코칭이 시급합니다.";
        }

        // 3. 둘 다 문제 (총체적 난국)
        if (req.getRiskType() == RiskType.BOTH) {
            return "출석과 성적 모두 심각한 상태로, 학업 지속 의지가 있는지 심층 상담이 필요합니다.";
        }

        return "전반적인 학업 및 출결 점검이 필요합니다.";
    }
}
