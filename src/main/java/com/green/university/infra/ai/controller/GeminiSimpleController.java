package com.green.university.infra.ai.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
public class GeminiSimpleController {

    private final WebClient webClient = WebClient.create("https://generativelanguage.googleapis.com");

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    @PostMapping("/gemini/simple")
    public Mono<String> callGemini(@RequestBody String input) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", "간단히 대답해줘:\n\n" + input))
                ))
        );

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/gemini-2.0-flash:generateContent")
                        .queryParam("key", apiKey)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

}
