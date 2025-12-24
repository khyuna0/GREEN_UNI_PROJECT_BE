package com.green.university.domain.counseling.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// 학생이 상담을 신청할 때 사용하는 DTO
@Setter
@Getter
public class CounselingStudentRequestDto {

    // 선택한 상담 일정 ID
    private Long counselingScheduleId;
    // 상담을 요청한 과목 ID
    private Long subjectId;
    // 상담 사유
    @NotNull(message = "상담 사유를 입력해주세요")
    private String reason;

}
