package com.green.university.domain.counseling.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CounselingProfessorOverallRequestDto {
    // 통합신청은 subjectId 없으니까 따로 dto 빼기

    @NotNull
    private Long studentId;

    @NotNull
    private Long counselingScheduleId;

    private String reason;
}
