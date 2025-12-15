package com.green.university.domain.professor.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * professor_tb insert용
 * @author 김지현
 *
 */
@Data
public class CreateProfessorFormDto {

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
	@Email
	private String email;
	private LocalDate hireDate;
	
}
