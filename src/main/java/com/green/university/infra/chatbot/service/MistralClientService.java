package com.green.university.infra.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class MistralClientService {

    private final WebClient webClient;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${app.ai.mistral.model:mistral-small-latest}")
    private String model;

    public MistralClientService(WebClient mistralWebClient) {
        this.webClient = mistralWebClient;
    }

    public String classifyToJson(String systemPrompt, String userPrompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.0
        );

        String raw = webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = om.readTree(raw);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "{\"intent\":\"OUT_OF_SCOPE\",\"mode\":\"OUT_OF_SCOPE\",\"confidence\":0.0,\"reason\":\"parse_fail\"}";
        }
    }

    /**
     * QA 답변 생성용 (자유 텍스트)
     */
    public String answer(String systemPrompt, String userPrompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.2
        );

        String raw = webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = om.readTree(raw);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "확인 중 오류가 발생했어요. 😅\n관련 메뉴에서 직접 확인해 주세요!";
        }
    }
}
