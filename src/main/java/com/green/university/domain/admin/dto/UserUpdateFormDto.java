package com.green.university.domain.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateFormDto {

    // 업데이트할 정보를 엔티티에 저장할 때 사용하는 Dto

	private Long userId;
    @NotBlank(message = "주소를 입력해 주세요")
	private String address;
    @NotBlank(message = "전화번호를 입력해 주세요")
    @Pattern(regexp = "^010-\\d{3,4}-\\d{4}$", message = "전화번호 양식(010-xxxx-xxxx)을 확인해주세요.")
    private String tel;
    @Email(message = "이메일 양식이 아닙니다.")
    @NotBlank(message = "이메일을 입력해 주세요")
	private String email;
	
}
