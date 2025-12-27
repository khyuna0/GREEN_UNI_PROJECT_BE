package com.green.university.infra.chatbot.intent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRouteResult {

    private ChatIntent intent;
    private String confidenceReason;
    private double confidence;
    private RouteMode mode;

    //  라우터에서 (intent, reason)만 반환할 수 있게
    public ChatRouteResult(ChatIntent intent, String confidenceReason) {
        this.intent = intent;
        this.confidenceReason = confidenceReason;
        this.confidence = 1.0;     // 기본값
        this.mode = RouteMode.QA;  // 기본값 (registry.get(intent, mode) 때문에 null이면 안됨)
    }
}
