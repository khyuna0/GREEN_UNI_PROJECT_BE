package com.green.university.infra.chatbot.intent;

public enum RouteMode {
    NAVIGATE,   // 링크 안내 가능한 확실한 경우
    QA,         // 애매하면 AI 설명만
    OUT_OF_SCOPE
}
