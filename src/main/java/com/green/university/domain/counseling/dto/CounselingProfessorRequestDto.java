package com.green.university.domain.counseling.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CounselingProfessorRequestDto {
    // 교수 -> 학생에게 상담신청

    @NotNull
    private Long studentId;

    @NotNull
    private Long counselingScheduleId;

    @NotNull
    private Long subjectId;

    private String reason; // 교수 요청 메시지(선택)

}
