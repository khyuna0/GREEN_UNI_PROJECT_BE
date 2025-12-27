package com.green.university.infra.ai.util;

import com.green.university.infra.ai.dto.AiRiskAnalysisRequest;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    // 학생 위험도 분석용 프롬프트 생성
    // 나중에 프롬프트 버전 관리할 때 여기만 수정
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
                - 위험 유형: %s
                - 위험 수준: %s
                
                [JSON 필수 키]
                { "summary": "학생 상태에 대한 날카로운 1줄 핵심 요약 (30자 이내)",
                  "professorGuide": "교수가 학생과 상담할 때 던져야 할 구체적인 조언 (경어체 사용, 120자 이내)",
                  "studentMessage": "학생에게 시스템 알림으로 보낼 따뜻하지만 경각심을 주는 격려의 문장 (2문장 이내)",
                  "reasonTags": ["태그1", "태그2"]  // 최대 3개, 아래 태그 목록에서만 선택
                }
                
                [사용 가능한 태그 (최대 3개 선택)]
                - 출석: "잦은결석", "결석경고", "지각누적"
                - 성적: "학점F", "성적저조", "성적우수"
                - 복합: "출석성적불일치", "이탈위험", "동기부족"
                
                [태그 선택 기준]
                - 결석 4회 이상 → "잦은결석"
                - 결석 3회 → "결석경고"
                - 지각 9회 이상 → "지각누적"
                - F학점 → "학점F"
                - 60점 미만 → "성적저조"
                - 90점 이상 → "성적우수"
                - 출석 좋은데 성적 나쁨 OR 반대 → "출석성적불일치"
                - BOTH + DANGER → "이탈위험"
                - WARNING + 복합 → "동기부족"

                [주의]
                summary, professorGuide, studentMessage 세 필드는 모두 반드시 문자열(String)로만 작성하세요.
                reasonTags는 반드시 배열 형태로, 위 목록에 없는 태그 사용 금지.
                객체나 배열 사용 금지.
                """.formatted(
                req.getStudentName(),
                req.getSubjectName(),
                req.getAbsent(),
                req.getLateness(),
                req.getConvertedMark(),
                req.getLetterGrade(),
                req.getRiskType(),
                req.getRiskLevel()
        );
    }
}
