package com.green.university.dto.response;

import com.green.university.entity.StuSub;
import com.green.university.entity.Subject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StuSubDayTimeDto {
    // 학생이 신청한 과목의 요일/시간 정보 -> 예비수강신청 시 검증용으로 사용하는 DTO

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

	// PreStuSub용 (예비 수강신청)
	public StuSubDayTimeDto(Subject s) {
		this.subjectId = s.getId();
		this.subjectName = s.getName();
		this.subDay = s.getSubDay();
		this.startTime = s.getStartTime();
		this.endTime = s.getEndTime();
	}

	//  StuSub용 (실제 수강신청)
	public static StuSubDayTimeDto fromEntity(StuSub stuSub) {
		return new StuSubDayTimeDto(stuSub.getSubject());
	}
}
