package com.green.university.repository.model;

import lombok.Data;

@Data
public class Schedule {

	private Long id;
	private Long staffId;
	private String startDay;
	private String endDay;
	private String information;
	private Long years;
	private Long months;
}
