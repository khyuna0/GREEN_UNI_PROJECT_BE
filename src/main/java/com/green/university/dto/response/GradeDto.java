package com.green.university.dto.response;

import lombok.Data;

@Data
public class GradeDto {
	
	private Long subYear;
	private Long semester;
	private Long subjectId;
	private Long evaluationId;
	private String name;
	private String type;
	private String grade;
	private String grades;
	private String gradeValue;
	
}
