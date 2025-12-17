package com.green.university.domain.counseling.dto;

import jakarta.validation.constraints.NotNull;

// 학생이 상담을 신청할 때 사용하는 DTO
public class CounselingStudentRequestDto {

    // 선택한 상담 일정 ID
    private Long counselingScheduleId;

    // 상담을 요청한 과목 ID
    private Long subjectId;

    // 상담 사유
    @NotNull(message = "상담 사유를 입력해주세요")
    private String reason;

    public Long getCounselingScheduleId() {
        return counselingScheduleId;
    }

    public void setCounselingScheduleId(Long counselingScheduleId) {
        this.counselingScheduleId = counselingScheduleId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
