package com.green.university.dto.response;

import lombok.Data;

@Data
public class SubjectForProfessorDto {

	private Long id;
	private String name;
	private String subDay;
	private Long startTime;
	private Long endTime;
	private String roomId;
	
}
