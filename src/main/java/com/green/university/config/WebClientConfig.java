package com.green.university.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

// application.yml에서 설정한 값들을 자바 클래스로 읽어오기 위한 첫번째 단계
// WebClient : 외부 API에 비동기로 요청을 보내고 응답을 받아오는 도구
@Configuration
public class WebClientConfig {

    // Gemini 설정값
    @Value("${app.ai.gemini.api-key}")
    private String geminiApiKey;

    @Value("${app.ai.gemini.url}")
    private String geminiBaseUrl;

    // Mistral 설정값
    @Value("${app.ai.mistral.api-key}")
    private String mistralApiKey;

    @Value("${app.ai.mistral.url}")
    private String mistralBaseUrl;

    // Gemini API 전용 WebClient 설정
    @Bean(name = "geminiWebClient")
    public WebClient geminiWebClient() {
        return WebClient.builder()
                .baseUrl(geminiBaseUrl)
                .defaultHeader("x-goog-api-key", geminiApiKey) // Gemini 필수 헤더
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // Mistral WebClient Bean
    @Bean(name = "mistralWebClient")
    public WebClient mistralWebClient() {
        return WebClient.builder()
                .baseUrl(mistralBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + mistralApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}