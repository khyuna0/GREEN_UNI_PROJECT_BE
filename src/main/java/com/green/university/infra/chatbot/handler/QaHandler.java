package com.green.university.infra.chatbot.handler;

import com.green.university.global.websocket.ChatHandler;
import com.green.university.global.websocket.dto.ChatContext;
import com.green.university.infra.chatbot.dto.ChatResponseDto;
import com.green.university.infra.chatbot.service.MistralClientService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QaHandler implements ChatHandler {

    private final PortalCatalog catalog;
    private final MistralClientService mistral;

    public QaHandler(PortalCatalog catalog, MistralClientService mistral) {
        this.catalog = catalog;
        this.mistral = mistral;
    }

    @Override
    public ChatResponseDto handle(ChatContext ctx) {
        // role에 맞는 topic만 추려서 “요약 지식팩” 만들기
        String role = ctx.getUserRole();

        // 간단히: catalog에 pageSummary 쭉 붙여서 systemPrompt로 제공
        // "너는 그린대 포털 안내원, 아래 정보 범위에서만 답해라" 스타일
        String systemPrompt =
                "너는 그린대학교 포털 안내 챗봇이다.\n" +
                        "아래 '페이지 요약 정보' 범위 내에서만 답변한다.\n" +
                        "모르는 내용은 '확인 필요'라고 말하고, 관련 메뉴를 안내한다.\n" +
                        "답변은 짧고 단계적으로(1,2,3) 작성한다.\n\n" +
                        "페이지 요약 정보:\n" +
                        catalog.buildSummaryPackForRole(role); // role 필터된 요약팩

        String userPrompt = ctx.getMessage();

        String answer = mistral.answer(systemPrompt, userPrompt); // answer 메서드 필요

        return new ChatResponseDto(answer, List.of(), List.of());
    }
}
