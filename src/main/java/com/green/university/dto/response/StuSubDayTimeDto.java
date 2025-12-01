package com.green.university.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StuSubDayTimeDto {

	private Long subjectId;
	private String subjectName;
	private String subDay;
	private Long startTime;
	private Long endTime;
	
	// startTime ~ endTime을 정수형 배열로 생성
	public List<Long> timeList() {
		List<Long> resultList = new ArrayList<>();
		
		for (Long i = startTime; i <= endTime; i++) {
			resultList.add(i);
		}
		return resultList;
	}
}
