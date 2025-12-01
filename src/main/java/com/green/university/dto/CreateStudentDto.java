package com.green.university.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.sql.Date;

/**
 * student_tb insert용
 * @author 김지현
 *
 */
@Data
public class CreateStudentDto {

	@NotEmpty
	@Size(min = 2, max= 30)
	private String name;
	private Date birthDate;
	private String gender;
	@NotEmpty
	private String address;
	@NotBlank
	private String tel;
	@Min(100)
	@Max(999)
	private Long deptId;
	private Date entranceDate;
	@Email
	private String email;
	
}
