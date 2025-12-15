package com.green.university.global.websocket;

import com.green.university.global.websocket.dto.ChatContext;
import com.green.university.infra.chatbot.dto.ChatResponseDto;

public interface ChatHandler {
    ChatResponseDto handle(ChatContext ctx);
}
