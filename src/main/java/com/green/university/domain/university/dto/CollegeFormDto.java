package com.green.university.domain.university.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 *
 * @author 박성희
 *
 */
@Data
public class CollegeFormDto {
    @NotBlank(message = "단과대 이름을 입력해 주세요")
    private String name;
}
