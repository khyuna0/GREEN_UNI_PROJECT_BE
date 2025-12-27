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

        public ChatResponseDto handle(String message, String userRole) {
            ChatRouteResult routed = router.route(message, userRole);

            // QA에서 role을 써야 하므로 ChatContext에 role 넣어줌
            ChatContext ctx = new ChatContext(message, userRole, routed);

            return registry.get(routed.getIntent(), routed.getMode()).handle(ctx); // mode로 분기 추천
        }
    }
