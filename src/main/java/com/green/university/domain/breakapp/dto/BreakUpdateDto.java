package com.green.university.domain.breakapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BreakUpdateDto {
    // 휴학신청 수정용 , 정말 수정할 내용만 들어가는게좋음
    @NotNull(message = "종료 년도를 입력해주세요")
    private Long toYear;

    @NotNull(message = "종료 학기를 입력해주세요")
    private Long toSemester;

    @NotNull(message = "휴학 구분을 입력해주세요")
    private String type;
}
