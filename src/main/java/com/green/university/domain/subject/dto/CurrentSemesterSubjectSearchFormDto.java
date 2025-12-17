package com.green.university.domain.subject.dto;

import com.green.university.global.utils.Define;
import com.green.university.global.utils.TermUtil;
import lombok.Data;

@Data
public class CurrentSemesterSubjectSearchFormDto {

	private String type; // 전공 또는 교양
	private Long deptId;
	private String deptName;
	private String name;
	
	private Long subYear = TermUtil.currentYear();
	private Long semester = TermUtil.currentSemester();

	
}
