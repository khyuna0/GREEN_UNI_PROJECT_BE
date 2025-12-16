package com.green.university.infra.chatbot.service;

import com.green.university.global.websocket.ChatRouter;
import com.green.university.global.websocket.dto.ChatContext;
import com.green.university.infra.chatbot.dto.ChatResponseDto;
import com.green.university.infra.chatbot.handler.HandlerRegistry;
import com.green.university.infra.chatbot.intent.ChatRouteResult;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatRouter router;
    private final HandlerRegistry registry;

    public ChatService(ChatRouter router, HandlerRegistry registry) {
        this.router = router;
        this.registry = registry;
    }

    public ChatResponseDto handle(String message) {
        ChatRouteResult routed = router.route(message);
        ChatContext ctx = new ChatContext(message, routed);
        return registry.get(routed.getIntent()).handle(ctx);
    }
}