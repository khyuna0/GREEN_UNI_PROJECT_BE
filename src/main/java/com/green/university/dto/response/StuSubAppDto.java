package com.green.university.dto.response;

import lombok.Data;

/**
 * @author 서영
 */
@Data
public class StuSubAppDto {

	private Long studentId;
	private Long subjectId;
	private String subjectName;
	private String professorName;
	private Long grades;

	private String subDay;
	private Long startTime;
	private Long endTime;
	private Long numOfStudent;
	private Long capacity;
	private String roomId;
	
	private Boolean status;
	
}
