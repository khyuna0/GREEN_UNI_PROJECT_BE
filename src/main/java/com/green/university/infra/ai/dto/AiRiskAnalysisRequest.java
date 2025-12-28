package com.green.university.infra.ai.dto;

import com.green.university.domain.dropoutrisk.entity.RiskLevel;
import com.green.university.domain.dropoutrisk.entity.RiskType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiRiskAnalysisRequest { // AI에게 넘기는 핵심 DTO

    private Long studentId;
    private String studentName;
    private Long subjectId;
    private String subjectName;

    private long absent;
    private long lateness;

    private Long homework;
    private Long midExam;
    private Long finalExam;

    private double convertedMark; // 환산 점수
    private String letterGrade;       // "A0", "F" 등
    private Double semesterGpa; // null 가능

    private RiskType riskType;
    private RiskLevel riskLevel;

}