package com.green.university.infra.ai.dto.response;

import com.green.university.global.utils.DateTimeUtil;
import com.green.university.domain.dropoutrisk.entity.DropoutRisk;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DropoutRiskRowDto {
    private Long id;

    private Long studentId;
    private String studentName;

    private Long subjectId;
    private String subjectName;
    private String professorName;

    private String riskType;
    private String riskLevel;
    private String status;

    private String aiSummary;
    private String aiRecommendation;
    private String aiReasonTags;

    private String updatedAt;

    public static DropoutRiskRowDto from(DropoutRisk r) {
        return DropoutRiskRowDto.builder()
                .id(r.getId())
                .studentId(r.getStuSub().getStudent().getId())
                .studentName(r.getStuSub().getStudent().getName())
                .subjectId(r.getStuSub().getSubject().getId())
                .subjectName(r.getStuSub().getSubject().getName())
                .professorName(r.getStuSub().getSubject().getProfessor().getName())
                .riskType(r.getRiskType() != null ? r.getRiskType().name() : null)
                .riskLevel(r.getRiskLevel() != null ? r.getRiskLevel().name() : null)
                .status(r.getStatus() != null ? r.getStatus().name() : null)
                .aiSummary(r.getAiSummary())
                .aiRecommendation(r.getAiRecommendation())
                .aiReasonTags(r.getAiReasonTags())
                .updatedAt(DateTimeUtil.dateTimeToString(r.getUpdatedAt()))
                .build();
    }
}
