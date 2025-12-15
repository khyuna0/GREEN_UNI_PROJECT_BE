package com.green.university.infra.ai.client;

import com.green.university.infra.ai.dto.response.GeminiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("geminiClient")
public class GeminiClient implements AiClient {

    @Qualifier("geminiWebClient")
    private final WebClient webClient;

    public GeminiClient(@Qualifier("geminiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String analyze(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        GeminiResponseDto response = webClient.post()
                .uri("/models/gemini-2.0-flash:generateContent")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode().value() == 429) {
                        log.warn("🚨 Gemini Rate Limit 걸림 - 재시도 중...");
                        return clientResponse.createException().flatMap(Mono::error);
                    }
                    return clientResponse.bodyToMono(String.class)
                            .map(body -> new RuntimeException("Gemini 4xx 에러: " + body));
                })
                .bodyToMono(GeminiResponseDto.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(10))
                        .filter(t -> t instanceof WebClientResponseException.TooManyRequests))
                .block();

        if (response == null) {
            throw new RuntimeException("Gemini 응답 null");
        }

        return response.extractText(); // DTO에 만든 헬퍼 메서드 사용
    }
}
