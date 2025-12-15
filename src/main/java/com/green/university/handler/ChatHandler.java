package com.green.university.handler;

import com.green.university.dto.response.ChatResponseDto;

public interface ChatHandler {
    ChatResponseDto handle(ChatContext ctx);
}
