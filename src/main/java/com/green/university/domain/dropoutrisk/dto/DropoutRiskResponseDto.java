package com.green.university.domain.dropoutrisk.dto;

import com.green.university.domain.dropoutrisk.entity.DropoutRisk;
import com.green.university.domain.dropoutrisk.entity.RiskLevel;
import com.green.university.domain.dropoutrisk.entity.RiskStatus;
import com.green.university.domain.dropoutrisk.entity.RiskType;
import com.green.university.global.utils.DateTimeUtil;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DropoutRiskResponseDto {

    private Long id;

    private Long studentId;
    private String studentName;

    private Long subjectId;
    private String subjectName;
    private String professorName;

    private RiskType riskType;
    private RiskLevel riskLevel;
    private RiskStatus status;

    private String aiSummary;
    private String aiRecommendation;
    private String aiStudentMessage;
    private String aiReasonTags;

    private String createdAt;
    private String updatedAt;


    public static DropoutRiskResponseDto fromEntity(DropoutRisk risk) {
        DropoutRiskResponseDto dto = new DropoutRiskResponseDto();
        dto.setId(risk.getId());

        dto.setStudentId(risk.getStuSub().getStudent().getId());
        dto.setStudentName(risk.getStuSub().getStudent().getName());

        dto.setSubjectId(risk.getStuSub().getSubject().getId());
        dto.setSubjectName(risk.getStuSub().getSubject().getName());
        dto.setProfessorName(risk.getStuSub().getSubject().getProfessor().getName());

        dto.setRiskType(risk.getRiskType());
        dto.setRiskLevel(risk.getRiskLevel());
        dto.setStatus(risk.getStatus());

        dto.setAiSummary(risk.getAiSummary());
        dto.setAiRecommendation(risk.getAiRecommendation());
        dto.setAiStudentMessage(risk.getAiStudentMessage());
        dto.setAiReasonTags(risk.getAiReasonTags());

        dto.setCreatedAt(DateTimeUtil.dateTimeToString(risk.getCreatedAt()));
        dto.setUpdatedAt(DateTimeUtil.dateTimeToString(risk.getUpdatedAt()));
        return dto;
    }
}

