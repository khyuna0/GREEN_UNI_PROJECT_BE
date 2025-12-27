package com.green.university.infra.chatbot.util;

public final class RoleNormalizer {

    private RoleNormalizer() {}

    // role 정규화
    public static String normalize(String role) {
        if (role == null) return "student";

        String r = role.trim().toLowerCase();

        if (r.contains("student")) return "student";
        if (r.contains("professor")) return "professor";
        if (r.contains("staff") || r.contains("admin")) return "staff";

        // 이미 student/professor/staff 형태면 그대로
        return r.isBlank() ? "student" : r;
    }
}
