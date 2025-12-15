package com.green.university.dto.response;

import com.green.university.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PreReserveDto { // 상담 예비 신청

    // 대상 교수
    @NotNull(message = "상담할 교수님을 선택해 주세요")
    private Long professorId;

    // 교수 오픈 일정
    @NotNull(message = "상담할 날짜를 선택해 주세요")
    private CounselingSchedule counselingSchedule;

    // 상담 사유
    @NotNull(message = "상담 사유를 입력해 주세요")
    @Size(max = 200, message = "상담 사유는 200자를 넘을 수 없습니다.")
    @Size(min = 10, message = "상담 사유는 10글자 이상 입력해 주세요")
    private String reason;
    
    
}

