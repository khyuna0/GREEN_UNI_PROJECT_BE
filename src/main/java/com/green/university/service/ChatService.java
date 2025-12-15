package com.green.university.service;

import com.green.university.dto.response.ChatResponseDto;
import com.green.university.handler.ChatContext;
import com.green.university.handler.ChatRouter;
import com.green.university.handler.HandlerRegistry;
import com.green.university.intent.ChatRouteResult;
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