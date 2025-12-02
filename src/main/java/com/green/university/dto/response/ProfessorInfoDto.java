package com.green.university.dto.response;

import lombok.Data;

import java.util.Date;

//import java.sql.Date; 타입 오류 때문에 위로 변경

@Data
public class ProfessorInfoDto {

	private Long id;
	private String name;
	private Date birthDate;
	private String gender;
	private String address;
	private String tel;
	private String email;
	private Long deptId;
	private Date hireDate;
	private String deptName;
	private String collegeName;
}
