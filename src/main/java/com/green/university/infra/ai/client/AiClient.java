package com.green.university.infra.ai.client;

// 인터페이스 만든 이유 : ChatGPT 추가할 때 Service 코드 안 건드려도 됨
// Service는 "어떤 AI 쓸지" 결정만, 호출 로직은 Client가 담당
public interface AiClient {
    /**
     * AI에게 프롬프트 보내고 JSON 응답 텍스트 받기
     * @param prompt 분석 요청 프롬프트
     * @return AI가 뱉은 JSON 문자열
     */
    String analyze(String prompt);
}

