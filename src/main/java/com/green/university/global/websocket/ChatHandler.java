package com.green.university.global.websocket;

import com.green.university.infra.chatbot.dto.ChatResponseDto;
import com.green.university.global.websocket.dto.ChatContext;

public interface ChatHandler {
    ChatResponseDto handle(ChatContext ctx);
}
