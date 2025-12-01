package com.green.university.dto;

import com.green.university.utils.NumberUtil;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
/**
 *
 * @author 박성희
 *
 */
@Data
public class CollTuitFormDto {
    //@NotBlank, @NotEmpty는 문자열용 -> NotNull로 변경

    @NotNull
    private Long collegeId;

    private String 	name; // 단과대 이름

    @NotNull
    @Positive //정수형에 써주는것
    private Long amount;

    public String amountFormat() {
        return NumberUtil.numberFormat(amount);
    }

}
