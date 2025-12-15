package com.green.university.domain.university.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 *
 * @author 박성희
 *
 */
@Data
public class DepartmentFormDto { // 학과 등록 시 사용하는 DTO

    private Long id; // 아이디는 자동증가 생성되는데 필요할까?

    @NotNull
    @NotBlank
    private String name; // 학과이름

    private Long collegeId; // 단과대 아이디

    @NotNull
    private String collegeName; // 단과대 이름
}
