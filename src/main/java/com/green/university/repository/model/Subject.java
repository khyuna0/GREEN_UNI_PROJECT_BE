package com.green.university.repository.model;

import lombok.Data;

@Data
public class Subject {
	private Long id;
	private String name;
	private Long professorId;
	private String roomId;
	private Long deptId;
	private String type;
	private Long subYear;
	private Long semester;
	private String subDay;
	private Long startTime;
	private Long endTime;
	private Long grades;
	private Long capacity;
	private Long numOfStudent;
}
