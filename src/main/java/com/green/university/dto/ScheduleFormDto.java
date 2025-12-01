package com.green.university.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleFormDto {

	private Long id;
	private Long staffId;
	private LocalDate startDay;
	private LocalDate endDay;
	private String information;
}
