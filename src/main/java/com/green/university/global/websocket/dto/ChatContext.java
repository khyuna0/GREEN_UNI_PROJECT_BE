package com.green.university.global.websocket.dto;

import com.green.university.infra.chatbot.intent.ChatRouteResult;
import lombok.Data;

@Data
public class ChatContext {

    private final String message;
    private final ChatRouteResult routed;

    public ChatContext(String message, ChatRouteResult routed) {
        this.message = message;
        this.routed = routed;
    }
}
