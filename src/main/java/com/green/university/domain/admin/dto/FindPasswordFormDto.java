package com.green.university.domain.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 비밀번호 찾기 폼
 * @author 김지현
 *
 */
@Data
public class FindPasswordFormDto {

	@NotBlank(message = "이름을 입력해주세요")
	private String name;
    @NotNull(message = "아이디를 입력해주세요")
    @Positive(message = "아이디는 숫자여야 합니다.")
	private Long id;
	@Email(message = "올바른 이메일 형식이 아닙니다")
    @NotBlank(message = "이메일을 입력해주세요")
	private String email;
    @NotBlank(message = "회원 유형을 선택해주세요.")
	private String userRole;
	
}
