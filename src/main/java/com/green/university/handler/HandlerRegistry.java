package com.green.university.handler;

import com.green.university.intent.ChatIntent;
import org.springframework.stereotype.Component;


@Component
public class HandlerRegistry {

    private final PortalHandler portalHandler;
    private final OutOfScopeHandler outOfScopeHandler;

    public HandlerRegistry(PortalHandler portalHandler, OutOfScopeHandler outOfScopeHandler) {
        this.portalHandler = portalHandler;
        this.outOfScopeHandler = outOfScopeHandler;
    }

    public ChatHandler get(ChatIntent intent) {
        if (intent == ChatIntent.OUT_OF_SCOPE || intent == ChatIntent.UNKNOWN) {
            return outOfScopeHandler;
        }
        return portalHandler;
    }
}
