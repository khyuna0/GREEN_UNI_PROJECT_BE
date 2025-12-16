package com.green.university.infra.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseDto {

    private String answer;                // 봇 메시지
    private List<ChatLinkFormDto> links;     // “바로가기” 버튼들
    private List<String> references;    // “포털 > … > …” 같은 참고 경로 텍스트


}

