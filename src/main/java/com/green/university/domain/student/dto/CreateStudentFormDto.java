package com.green.university.domain.student.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * student_tb insert용
 * @author 김지현
 *
 */
@Data
public class CreateStudentFormDto {

	@NotEmpty
	@Size(min = 2, max= 30)
	private String name;
	private LocalDate birthDate;
	private String gender;
	@NotEmpty
	private String address;
	@NotBlank
	private String tel;
	@Min(100)
	@Max(999)
	private Long deptId;
	private LocalDate entranceDate;
	@Email
	private String email;
	@NotBlank
	private String grade;
	@NotBlank
	private String semester;
	
}
