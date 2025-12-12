package com.green.university.dto;

import lombok.Data;

// 전체 강의 조회에서 사용하는 검색 폼 dto
@Data
public class AllSubjectSearchFormDto {

	private Long subYear;
	private Long semester;
	private String type;
	private String deptName;
	private String name;
	
}
