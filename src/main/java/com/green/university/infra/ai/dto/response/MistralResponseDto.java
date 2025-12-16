package com.green.university.infra.ai.dto.response;

import java.util.List;

// Mistral API 응답용 DTO (Record로 만들어서 불변성 final로 설정)
// 깊숙이 들어있는 답변 꺼내기 choices -> message -> content
public record MistralResponseDto(List<Choice> choices) {

    public String extractContent() {
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("Mistral 응답에 choices 없음");
        }
        return choices.get(0).message().content();
    }

    public record Choice(Message message) {}
    public record Message(String content) {}
}
