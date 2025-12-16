package com.green.university.infra.ai.entity;

// 위험 단계
public enum RiskLevel {
    WARNING, // 경고 (결석 2회)
    DANGER   // 위험 (결석 3회 이상, 학점 미달 -> AI 분석 대상)
}
