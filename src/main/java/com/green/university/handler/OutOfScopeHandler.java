package com.green.university.handler;

import com.green.university.dto.response.ChatResponseDto;
import com.green.university.intent.ChatIntent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutOfScopeHandler implements ChatHandler {

    @Override
    public ChatResponseDto handle(ChatContext ctx) {
        return new ChatResponseDto(
                "죄송하지만, 제가 도와드릴 수 있는 영역이 아니에요. 😅\n\n" +
                        "저는 그린대학교 학사/대학생활 관련 정보를 안내하는 챗봇이에요!\n" +
                        "학교 관련해서 궁금하신 점이 있으시면 언제든지 물어봐주세요:)",
                List.of(),
                List.of()
        );
    }
}
