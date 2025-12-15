package com.green.university.domain.schedule.dto;

import com.green.university.domain.schedule.entity.Schedule;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleDto {

	private Long months; // 월별 학사 일정을 구하기 위해 넣어둔 필드인가봄
	private Long sum;
	private Long id;
	private Long staffId;
	private Long years;
	private String startMday;
	private String endMday;
	private LocalDate startDay; // 시작 날짜
	private LocalDate endDay; // 종료 날짜
	private String information; // 학사 일정 내용


    public ScheduleDto(Schedule schedule) {
        this.id = schedule.getId();
        this.startDay = schedule.getStartDay();
        this.endDay = schedule.getEndDay();
        this.staffId = schedule.getStaff().getId();
        this.information = schedule.getInformation();
    }

}

