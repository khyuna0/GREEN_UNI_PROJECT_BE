package com.green.university.domain.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleFormDto { // 학사 일정 등록, 수정 시 변경할 값을 다루는 DTO

	private Long staffId;

	@NotNull(message = "시작 날짜를 입력해주세요.")
	private LocalDate startDay;
	@NotNull(message = "종료 날짜를 입력해주세요.")
	private LocalDate endDay;

	@NotBlank (message = "내용을 입력해주세요.")
	private String information;
}
