package com.green.university.infra.ai.dto.response;

import com.green.university.infra.ai.entity.DropoutRisk;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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
    private String aiStudentMessage;
    private String aiReasonTags;

    private LocalDateTime updatedAt;

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
                .aiStudentMessage(r.getAiStudentMessage())
                .aiReasonTags(r.getAiReasonTags())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
