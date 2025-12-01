package com.green.university.dto.response;

import lombok.Data;

import java.sql.Date;

@Data
public class StudentInfoDto {

	private Long id;
	private String name;
	private Date birthDate;
	private String gender;
	private String address;
	private String tel;
	private String email;
	private Long deptId;
	private Long grade;
	private Long semester;
	private Date entranceDate;
	private Date graduationDate;
	private String deptName;
	private String collegeName;
}
