package com.green.university.dto.response;

import lombok.Data;
import java.util.List;

// 구글 Gemini API 응답용 DTO
// 구글이 보내주는 전체 JSON 구조 매핑
@Data
public class GeminiResponseDto {
    private List<Candidate> candidates;

    @Data
    public static class Candidate {
        private Content content;
    }

    @Data
    public static class Content {
        private List<Part> parts;
    }

    @Data
    public static class Part {
        private String text; // AI가 뱉은 실제 말(우리가 필요한 JSON 문자열이 여기 들어있음)
    }
}
