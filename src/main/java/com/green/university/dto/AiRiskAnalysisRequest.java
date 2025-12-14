package com.green.university.dto;

import com.green.university.entity.RiskLevel;
import com.green.university.entity.RiskType;
import lombok.Data;

@Data
public class AiRiskAnalysisRequest { // AI에게 넘기는 핵심 DTO

    private Long studentId;
    private String studentName;
    private Long subjectId;
    private String subjectName;

    private long absent;
    private long lateness;
    private double convertedMark;
    private String grade;       // "A0", "F" 등
    private Double semesterGpa; // null 가능

    private RiskType riskType;
    private RiskLevel riskLevel;

    // 추가: 과제 점수, 중간/기말 점수 등 넣어도 됨
}