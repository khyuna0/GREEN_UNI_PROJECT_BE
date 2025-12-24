package com.green.university.infra.chatbot.dto;

import lombok.Data;


@Data
public class ChatFormDto {

    private String message;
    private String userRole; // "student" | "professor" | "staff"

}
