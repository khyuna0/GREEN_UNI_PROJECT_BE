package com.green.university.domain.admin.dto;

import lombok.Data;

import jakarta.validation.constraints.Size;


@Data
public class ChangePasswordFormDto {

	@Size(min = 6, max = 20, message = "비밀번호는 6~20자 사이여야합니다.")
	private String beforePassword;
	@Size(min = 6, max = 20, message = "비밀번호는 6~20자 사이여야합니다.")
	private String afterPassword;
	@Size(min = 6, max = 20, message = "비밀번호는 6~20자 사이여야합니다.")
	private String passwordCheck;
	private Long id;
	
}
