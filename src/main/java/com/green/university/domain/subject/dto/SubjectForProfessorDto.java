package com.green.university.domain.subject.dto;

import lombok.Data;
/**
* 교수가 본인 과목 정보를 확인 하기 위한 용도
* */
@Data
public class SubjectForProfessorDto {

	private Long id;
	private String name;
	private String subDay;
	private Long startTime;
	private Long endTime;
	private String roomId;
	
}
