package com.green.university.global.websocket.dto;

import com.green.university.infra.chatbot.intent.ChatRouteResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatContext {

    private String message;

    // QA에서 role 기반 요약팩 만들려면 컨텍스트에 role이 있어야 함
    private String userRole;
    private ChatRouteResult routed;

    // 기존 코드 호환용(예전 생성자 호출 유지하려면)
    public ChatContext(String message, ChatRouteResult routed) {
        this.message = message;
        this.routed = routed;
    }
}
