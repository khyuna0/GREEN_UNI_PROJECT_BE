package com.green.university.infra.ai.client;

import com.green.university.infra.ai.dto.response.MistralResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component("mistralClient")
public class MistralClient implements AiClient {

    @Qualifier("mistralWebClient")
    private final WebClient webClient;

    public MistralClient(@Qualifier("mistralWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String analyze(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", "mistral-small-latest",
                "messages", List.of(Map.of("role", "user", "content", prompt + "\n\nJSON으로만 답변해.")),
                "response_format", Map.of("type", "json_object")
        );

        MistralResponseDto response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(MistralResponseDto.class)
                .block();

        if (response == null) {
            throw new RuntimeException("Mistral 응답 null");
        }

        return response.extractContent();
    }
}
