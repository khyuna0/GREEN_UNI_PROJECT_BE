package com.green.university.domain.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * staff_tb insert용
 * @author 김지현
 *
 */
@Data
public class CreateStaffFormDto {

	@NotEmpty
	@Size(min = 2, max= 30)
	private String name;
	private LocalDate birthDate;
	private String gender;
	@NotEmpty
	private String address;
	@Size(min = 11, max = 13)
	private String tel;
	@Email
	private String email;
	private LocalDate hireDate;
	
}
