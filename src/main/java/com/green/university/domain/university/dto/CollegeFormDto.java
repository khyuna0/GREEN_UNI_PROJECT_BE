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
    @NotBlank
    private String name;
}
