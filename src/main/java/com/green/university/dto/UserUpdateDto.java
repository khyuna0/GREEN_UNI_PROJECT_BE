package com.green.university.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Data
public class UserUpdateDto {

    // 업데이트할 정보를 엔티티에 저장할 때 사용하는 Dto

	private Long userId;
	@NotEmpty
	private String address;
	@NotBlank
	private String tel;
	@Email
	private String email;
	
}
