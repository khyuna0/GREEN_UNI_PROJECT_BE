package com.green.university.dto;

import lombok.Data;

@Data
public class ScheduleFormDto {

	private Long id;
	private Long staffId;
	private String startDay;
	private String endDay;
	private String information;
}
