package com.green.university.dto;

import com.green.university.entity.Schedule;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class ScheduleDto {

	private Long mouths;
	private Long sum;
	private Long id;
	private Long staffId;
	private Long years;
	private String startMday;
	private String endMday;
	private LocalDate startDay;
	private LocalDate endDay;
	private String information;


    public ScheduleDto(Schedule schedule) {
        this.id = schedule.getId();
        this.startDay = schedule.getStartDay();
        this.endDay = schedule.getEndDay();
        this.staffId = schedule.getStaff().getId();
        this.information = schedule.getInformation();
    }

}

