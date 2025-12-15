package com.green.university.infra.ai.service;

import com.green.university.infra.ai.client.AiClient;
import com.green.university.infra.ai.dto.AiRiskAnalysisRequest;
import com.green.university.infra.ai.dto.response.AiRiskAnalysisResult;
import com.green.university.infra.ai.util.PromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    @Qualifier("geminiClient")
    private final AiClient geminiClient;  // 우선 사용할 AI
    @Qualifier("mistralClient")
    private final AiClient mistralClient; // 백업 AI

    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper; // JSON 파싱용 (Spring에 기본 내장됨)


    public AiRiskAnalysisResult analyzeRisk(AiRiskAnalysisRequest request) {
        String prompt = promptBuilder.buildRiskAnalysisPrompt(request);

        try {
            // 1차 시도: Gemini
            String responseText = geminiClient.analyze(prompt);
            return parseJson(responseText);

        } catch (Exception e) {
            log.warn("⚠ Primary AI(Gemini) 실패: {}. Fallback 시도...", e.getMessage());

            try {
                // 2차 시도: Mistral
                String responseText = mistralClient.analyze(prompt);
                return parseJson(responseText);

            } catch (Exception ex) {
                log.error("❌ 모든 AI 실패", ex);
                return createErrorResponse(); // custom한 error 메시지 반환 (아래 헬퍼에 존재)
            }
        }
    }


    // --- [공통] 프롬프트 & 파싱 ---
    private String buildPrompt(AiRiskAnalysisRequest req) {
        return "당신은 20년차 대학 학사 상담 전문가입니다. 아래 학생 데이터를 분석해 중도 이탈 위험을 진단하고 JSON 포맷으로 응답하세요.\n" +
                "불필요한 서론이나 마크다운(```)은 삭제하세요.\n" +
                "[학생 정보]\n" +
                "- 이름: " + req.getStudentName() + "\n" +
                "- 과목: " + req.getSubjectName() + "\n" +
                "- 결석/지각: " + req.getAbsent() + "회 / " + req.getLateness() + "회\n" +
                "- 점수/등급: " + req.getConvertedMark() + "점 / " + req.getGrade() + "\n\n" +
                "[JSON 필수 키]\n" +
                "{ \"summary\": \"한줄요약\", \"professorGuide\": \"교수 가이드\", \"studentMessage\": \"학생 메시지\", \"reasonTags\": [\"태그1\"] }";
    }


    // ============== 헬퍼 메서드들 ==============
    private AiRiskAnalysisResult parseJson(String rawText) {
        String cleanJson = stripToJson(rawText);
        try {
            return objectMapper.readValue(cleanJson, AiRiskAnalysisResult.class);
        } catch (Exception e) {
            log.error("JSON 파싱 에러. 원문: {}", rawText);
            throw new RuntimeException("AI 응답이 JSON 형식 아님", e);
        }
    }


    // AI가 보낸 텍스트가 코드펜스나 설명과 섞여 있어도 순수 JSON 부분만 추출
    private static String stripToJson(String s) {
        String t = s == null ? "" : s.trim();
        //if (t.isEmpty()) return t;
        if (t.startsWith("```")) {
            int first = t.indexOf('{');
            int last = t.lastIndexOf('}');
            if (first >= 0 && last > first) return t.substring(first, last + 1);
        }
        // 혹시 앞뒤로 잡다한 텍스트가 있을 경우 대비
        int first = t.indexOf('{');
        int last = t.lastIndexOf('}');
        if (first >= 0 && last > first) return t.substring(first, last + 1);
        return t;
    }


    private AiRiskAnalysisResult createErrorResponse() {
        return new AiRiskAnalysisResult(
                "AI 분석 실패",
                "시스템 과부하로 분석 불가",
                "잠시 후 다시 시도해주세요.",
                Collections.emptyList()
        );
    }
}
