package com.green.university.infra.ai.dto.response;

import java.util.List;


// 구글 Gemini API 응답용 DTO (Record로 만들어서 불변성 final로 설정)
// 구글이 보내주는 전체 JSON 구조 매핑해줌
// AI가 뱉은 실제 말 (우리가 필요한 JSON 문자열은) candidates -> content -> parts -> text 여기에 담겨 있음
public record GeminiResponseDto(List<Candidate> candidates) {

    // 깊숙이 박힌 text 꺼내는 헬퍼 메서드 (null 방어 포함)
    public String extractText() {
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("Gemini 응답에 candidates 없음");
        }
        return candidates.get(0).content().parts().get(0).text();
    }

    public record Candidate(Content content) {}
    public record Content(List<Part> parts) {}
    public record Part(String text) {}
}