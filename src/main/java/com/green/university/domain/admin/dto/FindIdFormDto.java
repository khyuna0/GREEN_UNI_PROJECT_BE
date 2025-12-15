package com.green.university.domain.admin.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * id 찾기 폼
 * @author 김지현
 *
 */
@Data
public class FindIdFormDto {

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    @Email(message = "올바른 이메일 형식을 입력해주세요.")
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "회원 유형을 선택해주세요.")
    private String userRole;
}
