package com.green.university.infra.ai.dto.response;

import com.green.university.infra.ai.entity.DropoutRisk;
import com.green.university.infra.ai.entity.RiskLevel;
import com.green.university.infra.ai.entity.RiskStatus;
import com.green.university.infra.ai.entity.RiskType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DropoutRiskResponseDto {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long subjectId;
    private String subjectName;

    private RiskType riskType;
    private RiskLevel riskLevel;
    private RiskStatus status;

    private String aiSummary;
    private String aiRecommendation;
    private String aiStudentMessage;
    private String aiReasonTags;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DropoutRiskResponseDto fromEntity(DropoutRisk risk) {
        DropoutRiskResponseDto dto = new DropoutRiskResponseDto();
        dto.setId(risk.getId());
        dto.setStudentId(risk.getStuSub().getStudent().getId());
        dto.setStudentName(risk.getStuSub().getStudent().getName());
        dto.setSubjectId(risk.getStuSub().getSubject().getId());
        dto.setSubjectName(risk.getStuSub().getSubject().getName());
        dto.setRiskType(risk.getRiskType());
        dto.setRiskLevel(risk.getRiskLevel());
        dto.setStatus(risk.getStatus());
        dto.setAiSummary(risk.getAiSummary());
        dto.setAiRecommendation(risk.getAiRecommendation());
        dto.setAiStudentMessage(risk.getAiStudentMessage());
        dto.setAiReasonTags(risk.getAiReasonTags());
        dto.setCreatedAt(risk.getCreatedAt());
        dto.setUpdatedAt(risk.getUpdatedAt());
        return dto;
    }
}

