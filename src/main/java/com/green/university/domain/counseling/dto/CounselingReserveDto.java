package com.green.university.domain.counseling.dto;

import lombok.Data;

@Data
public class CounselingReserveDto {

    private Long preReserveId; // 예비 예약 아이디

    private Long studentId; // 예약 학생 아이디

    private Long subjectId; // 상담 요청 과목

    private String decision; // 승인 또는 반려
}
