package com.green.university.domain.university.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 *
 * @author 박성희
 *
 */
@Data
public class DepartmentFormDto { // 학과 등록 시 사용하는 DTO

    private Long id; // 아이디는 자동증가 생성되는데 필요할까?

    @NotNull(message = "학과 이름을 입력해주세요")
    @Pattern(
            regexp = "^[가-힣]+과$",
            message = "학과명은 한글만 입력 가능하며 '~과'로 끝나야 합니다"
    )
    private String name; // 학과이름

    private Long collegeId; // 단과대 아이디

    @NotNull(message = "단과대 이름을 입력해주세요")
    private String collegeName; // 단과대 이름
}
