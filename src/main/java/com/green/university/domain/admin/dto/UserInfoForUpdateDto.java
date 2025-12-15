package com.green.university.domain.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UserInfoForUpdateDto { 
    
    // 유저 정보를 업데이트 하기 전!!! 정보를 확인할 때 부르는 DTO (업데이트, 엔티티 저장 용 DTO는 따로 있다)
    // 직원, 교수, 학생 구분 없음
    
	@NotBlank
	private String address;
	@Size(min = 11, max = 13)
	private String tel;
	@Email
	private String email;
	
}
