package com.green.university.infra.chatbot.intent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRouteResult {

    private ChatIntent intent;
    private String confidenceReason; //디버깅용


}
