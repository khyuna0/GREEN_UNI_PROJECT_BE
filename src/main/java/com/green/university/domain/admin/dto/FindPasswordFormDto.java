package com.green.university.domain.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 비밀번호 찾기 폼
 * @author 김지현
 *
 */
@Data
public class FindPasswordFormDto {

	@NotBlank
	private String name;
	@Min(100000)
	private Long id;
	@Email
	private String email;
	private String userRole;
	
}
