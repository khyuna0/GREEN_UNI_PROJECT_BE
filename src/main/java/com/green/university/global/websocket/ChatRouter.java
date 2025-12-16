package com.green.university.global.websocket;

import com.green.university.infra.chatbot.service.MistralClientService;
import com.green.university.infra.chatbot.handler.PortalCatalog;
import com.green.university.infra.chatbot.intent.ChatIntent;
import com.green.university.infra.chatbot.intent.ChatRouteResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.List;


@Component
public class ChatRouter {

    private final MistralClientService mistral;
    private final PortalCatalog catalog;
    private final ObjectMapper om = new ObjectMapper();


    public ChatRouter(MistralClientService mistral, PortalCatalog catalog) {
        this.mistral = mistral;
        this.catalog = catalog;
    }

    public ChatRouteResult route(String message) {
        String m = normalize(message);


        // 규칙(키워드) 기반 라우팅: 가장 정확하고 빠름
        //  긴 키워드 우선 매칭(“휴학 내역”이 “휴학”보다 우선)
        ChatIntent ruleHit = ruleMatchByCatalog(m);
        if (ruleHit != null) {
            return new ChatRouteResult(ruleHit, "rule:catalog_keyword");
        }

        // 3) Mistral에게 intent 분류 요청(애매한 케이스만)
        String systemPrompt =
                "너는 '그린대학교 포털' 안내 챗봇의 라우터다.\n" +
                        "사용자의 질문을 아래 intent 중 하나로만 분류해라.\n" +
                        "출력은 반드시 JSON 하나로만 반환해라. (json_object)\n\n" +
                        "intent 후보:\n" + catalog.intentDescriptions() + "\n" +
                        "JSON 출력 스키마:\n" +
                        "{ \"intent\": \"<INTENT>\", \"reason\": \"<짧은 근거>\" }\n\n" +
                        "규칙:\n" +
                        "- 포털 기능과 무관하면 intent=OUT_OF_SCOPE\n" +
                        "- 애매하면 OUT_OF_SCOPE\n" +
                        "- 이유는 짧게\n";

        String userPrompt = "사용자 질문: " + message;

        String json = mistral.classifyToJson(systemPrompt, userPrompt);

        // 4) JSON 파싱 -> ChatRouteResult
        try {
            JsonNode node = om.readTree(json);
            String intentStr = node.path("intent").asText("OUT_OF_SCOPE");
            String reason = node.path("reason").asText("");

            ChatIntent intent;
            try {
                intent = ChatIntent.valueOf(intentStr);
            } catch (Exception e) {
                intent = ChatIntent.OUT_OF_SCOPE;
            }

            return new ChatRouteResult(intent, reason);
        } catch (Exception e) {
            return new ChatRouteResult(ChatIntent.OUT_OF_SCOPE, "parse_fail");
        }
    }

    /**
     * Catalog에 있는 Topic 키워드로 intent 매칭
     * - "휴학 내역" > "휴학"처럼 긴 표현 우선
     */
    private ChatIntent ruleMatchByCatalog(String normalizedMessage) {

        // Topic들을 키워드 최대 길이 기준으로 정렬(긴 표현 우선)
        List<PortalCatalog.Topic> topics = catalog.topicList();
        topics.sort(
                Comparator.comparingInt((PortalCatalog.Topic t) ->
                        t.keywords().stream()
                                .mapToInt(k -> normalize(k).length())
                                .max()
                                .orElse(0)
                ).reversed()
        );

        // 휴학처럼 애매한 건 여기서 미리 가르는 게 안정적
        if (normalizedMessage.contains(normalize("휴학"))) {
            if (containsAny(normalizedMessage, List.of("내역", "조회", "신청내역"))) {
                return ChatIntent.BREAK_LIST_STUDENT;
            }
            if (containsAny(normalizedMessage, List.of("처리", "승인", "관리"))) {
                return ChatIntent.BREAK_LIST_STAFF;
            }
            return ChatIntent.BREAK_APP;
        }

        // 일반 키워드 탐색
        for (PortalCatalog.Topic t : topics) {
            for (String kw : t.keywords()) {
                if (normalizedMessage.contains(normalize(kw))) {
                    return t.intent();
                }
            }
        }
        return null;
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase().replace(" ", "");
    }

    private boolean containsAny(String normalizedMessage, List<String> keywords) {
        for (String k : keywords) {
            if (normalizedMessage.contains(normalize(k))) return true;
        }
        return false;
    }
}