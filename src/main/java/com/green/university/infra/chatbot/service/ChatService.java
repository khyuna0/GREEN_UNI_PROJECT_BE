package com.green.university.infra.chatbot.service;

import com.green.university.global.websocket.ChatRouter;
import com.green.university.global.websocket.dto.ChatContext;
import com.green.university.infra.chatbot.dto.ChatResponseDto;
import com.green.university.infra.chatbot.handler.HandlerRegistry;
import com.green.university.infra.chatbot.intent.ChatIntent;
import com.green.university.infra.chatbot.intent.ChatRouteResult;
import com.green.university.infra.chatbot.intent.RouteMode;
import com.green.university.infra.chatbot.util.RoleNormalizer;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatRouter router;
    private final HandlerRegistry registry;

    public ChatService(ChatRouter router, HandlerRegistry registry) {
        this.router = router;
        this.registry = registry;
    }

    private ChatRouteResult staffOverride(String message, String role) {
        if (!"staff".equals(role)) return null;
        if (message == null) return null;

        String m = message.trim();

        boolean hasBreak = m.contains("휴학");
        if (!hasBreak) return null;

        boolean isApply = m.contains("신청");
        if (isApply) {
            ChatRouteResult r = new ChatRouteResult(ChatIntent.BREAK_APP, "staff asked break apply explicitly");
            r.setMode(RouteMode.NAVIGATE);
            r.setConfidence(1.0);
            return r;
        }

        ChatRouteResult r = new ChatRouteResult(ChatIntent.BREAK_LIST_STAFF, "staff asked break related -> default to staff handling");
        r.setMode(RouteMode.NAVIGATE);
        r.setConfidence(1.0);
        return r;
    }

    public ChatResponseDto handle(String message, String userRole) {
        // role 정규화는 백엔드에서 항상 처리 (프론트 중복 제거)
        String role = RoleNormalizer.normalize(userRole);

        // staff 휴학 관련은 우선 규칙으로 강제 라우팅
        ChatRouteResult forced = staffOverride(message, role);
        ChatRouteResult routed = (forced != null) ? forced : router.route(message, role);

        // QA에서 role을 써야 하므로 ChatContext에 role 넣어줌
        ChatContext ctx = new ChatContext(message, role, routed);

        return registry.get(routed.getIntent(), routed.getMode()).handle(ctx); // mode로 분기 추천
    }
}
