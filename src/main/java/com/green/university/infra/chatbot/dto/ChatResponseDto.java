package com.green.university.infra.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseDto {

    private String answer;                 // 봇 메시지
    private List<Link> links;              // “바로가기” 버튼들
    private List<String> references;       // “포털 > … > …” 같은 참고 경로 텍스트

    /**
     * 기존 ChatLinkFormDto를 응답 DTO 내부로 합침
     * - DTO 파일 수 줄이기
     * - 응답에서만 쓰이는 타입을 응답 안으로 캡슐화
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Link {
        private String label; // 버튼 텍스트
        private String path;  // 라우터 이동경로
    }
}
