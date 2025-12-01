package com.green.university.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;


/**
 * @author 서영
 *
 */

@Data
public class BreakAppFormDto {
	
	private Long studentId;
	private Long studentGrade;
	private Long fromYear;
	private Long fromSemester;
	@NotNull
	private Long toYear;
	@NotNull
	private Long toSemester;
	@NotNull
	private String type;
	
}
