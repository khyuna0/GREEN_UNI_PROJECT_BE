package com.green.university.dto.response;

import lombok.Data;

@Data
public class PrincipalDto {

	private Long id;
	private String password;
	private String userRole;
	private String name;
	
}
