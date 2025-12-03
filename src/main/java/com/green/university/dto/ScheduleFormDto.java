package com.green.university.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleFormDto { // 학사 일정 수정 시 변경할 값을 다루는 DTO

	private Long staffId;
	private LocalDate startDay;
	private LocalDate endDay;
	private String information;
}
