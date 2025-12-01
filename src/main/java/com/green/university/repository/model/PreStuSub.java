package com.green.university.repository.model;

import lombok.Data;

@Data
public class PreStuSub {

	private Long studentId;
	private Long subjectId;
	
	public PreStuSub(Long studentId, Long subjectId) {
		this.studentId = studentId;
		this.subjectId = subjectId;
	}

	public PreStuSub() {
	}
	
}
