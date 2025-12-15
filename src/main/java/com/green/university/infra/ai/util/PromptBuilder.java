package com.green.university.infra.ai.util;

import com.green.university.infra.ai.dto.AiRiskAnalysisRequest;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    // 학생 위험도 분석용 프롬프트 생성
    // 나중에 프롬프트 버전 관리하거나 A/B 테스트(..가 뭐야..?)할 때 여기만 수정
    public String buildRiskAnalysisPrompt(AiRiskAnalysisRequest req) {
        return """
                당신은 20년 이상 경력의 대학 학사 경고 전문 상담 컨설턴트입니다.
                아래 학생 데이터를 분석해 중도 이탈(자퇴/제적) 위험을 진단하고 JSON 포맷으로 응답하세요.
                불필요한 서론이나 마크다운(```)은 삭제하세요.
                
                [학생 정보]
                - 이름: %s
                - 과목: %s
                - 결석/지각: %d회 / %d회
                - 점수/등급: %.1f점 / %s
                
                [JSON 필수 키]
                { "summary": "학생 상태에 대한 날카로운 1줄 핵심 요약",
                  "professorGuide": "교수가 학생과 상담할 때 던져야 할 구체적인 조언 (경어체 사용)",
                  "studentMessage": "학생에게 시스템 알림으로 보낼 따뜻하지만 경각심을 주는 격려 메시지",
                  "reasonTags": "위험 원인 태그 배열 (예: ['잦은결석', '학업성취도저하', '중도포기위험'])" }

                [주의]
                summary, professorGuide, studentMessage 세 필드는 모두 반드시 문자열(String)로만 작성하세요.
                객체나 배열 사용 금지.
                """.formatted(
                req.getStudentName(),
                req.getSubjectName(),
                req.getAbsent(),
                req.getLateness(),
                req.getConvertedMark(),
                req.getGrade()

                // 필요할까?
                //        sb.append("- 시스템 감지 위험 유형: ").append(req.getRiskType()).append("\n");
                //        sb.append("- 시스템 감지 위험 레벨: ").append(req.getRiskLevel()).append("\n\n");
        );
    }
}
