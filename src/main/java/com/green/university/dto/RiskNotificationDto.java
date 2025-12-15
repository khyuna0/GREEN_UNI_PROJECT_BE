package com.green.university.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RiskNotificationDto {
    private Long riskId;
    private String studentName;
    private String subjectName;
    private String message;
    private String severity; // "WARNING" or "DANGER"
    private String targetRole; // "STUDENT" or "PROFESSOR"
}
