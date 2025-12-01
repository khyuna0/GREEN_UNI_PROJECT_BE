package com.green.university.repository.model;

import lombok.Data;

@Data
public class StuSub {

	private Long id;
	private Long studentId;
	private Long subjectId;
	private String grade;
	private Long completeGrade;
}
