package com.green.university.infra.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Component
public class MistralClientService {


    private final WebClient webClient;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${app.ai.mistral.url}")
    private String mistralUrl; // 풀 URL 그대로

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
                .uri(mistralUrl)   // URL 호출
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = om.readTree(raw);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "{\"intent\":\"OUT_OF_SCOPE\",\"reason\":\"parse_fail\"}";
        }
    }
}