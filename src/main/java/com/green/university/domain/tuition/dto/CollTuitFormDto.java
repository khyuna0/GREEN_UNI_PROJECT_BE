package com.green.university.domain.tuition.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 *
 * @author 박성희
 *
 */
@Data
public class CollTuitFormDto {


    private Long collegeId;

    private String 	name; // 단과대 이름 -> 이름으로 검색

    @NotNull
    @Positive(message = "등록금은 숫자만 입력 가능합니다")
    private Long amount;

}
