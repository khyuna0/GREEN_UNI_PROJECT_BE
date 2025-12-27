package com.green.university.infra.ai.service;

import com.green.university.infra.ai.client.AiClient;
import com.green.university.infra.ai.dto.AiRiskAnalysisRequest;
import com.green.university.infra.ai.dto.AiRiskAnalysisResult;
import com.green.university.infra.ai.util.PromptBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
// 외부 AI API(Gemini, Mistral) 호출 + JSON 파싱만 담당
public class AiAnalysisService {

    @Qualifier("geminiClient")
    private final AiClient geminiClient;  // 우선 사용할 AI
    @Qualifier("mistralClient")
    private final AiClient mistralClient; // 백업 AI

    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper; // JSON 파싱용 (Spring에 기본 내장됨)

    public AiAnalysisService(AiClient geminiClient, AiClient mistralClient, PromptBuilder promptBuilder, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.mistralClient = mistralClient;
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
    }

    // ai로 risk 분석하기 (실제로 DropoutRiskService에서 사용 할 메서드)
    public AiRiskAnalysisResult analyzeRisk(AiRiskAnalysisRequest request) {
        String prompt = promptBuilder.buildRiskAnalysisPrompt(request);
        return callAiWithFallback(prompt);
    }

    // ai로 risk 분석할 때 ai call 하기
    private AiRiskAnalysisResult callAiWithFallback(String prompt) {
        try {
            // 1차 시도: Mistral
            String responseText = mistralClient.analyze(prompt);
            return parseJson(responseText);

        } catch (Exception e) {
            log.warn("⚠ Primary AI(Mistral) 실패: {}. Fallback 시도...", e.getMessage());

            try {
                // 2차 시도: Gemini
                String responseText = geminiClient.analyze(prompt);
                return parseJson(responseText);

            } catch (Exception ex) {
                log.error("❌ 모든 AI 실패", ex);
                throw new RuntimeException("AI 분석 API 호출 실패: " + ex.getMessage(), ex);
            }
        }
    }


    // ============== 헬퍼 메서드들 ==============
    private static final Set<String> VALID_TAGS = Set.of(
            "잦은결석", "결석경고", "지각누적",
            "학점F", "성적저조", "성적우수",
            "출석성적불일치", "이탈위험", "동기부족"
    );


    private AiRiskAnalysisResult parseJson(String rawText) {
        String cleanJson = stripToJson(rawText);
        try {
            ObjectMapper mapper = new ObjectMapper();
            AiRiskAnalysisResult result = mapper.readValue(cleanJson, AiRiskAnalysisResult.class);
            // ✅ 태그 검증 (유효한 태그만 필터링)
            if (result.getReasonTags() != null) {
                List<String> validTags = result.getReasonTags().stream()
                        .filter(VALID_TAGS::contains)
                        .limit(3) // 최대 3개
                        .collect(Collectors.toList());
                result.setReasonTags(validTags);
            }
            return result;
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

}
