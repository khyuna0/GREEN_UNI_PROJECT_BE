package com.green.university.dto;

import com.green.university.utils.Define;
import lombok.Data;

@Data
public class CurrentSemesterSubjectSearchFormDto {

	private String type;
	private Long deptId;
	private String name;
	
	private Long subYear = Define.CURRENT_YEAR;
	private Long semester = Define.CURRENT_SEMESTER;

	
}
