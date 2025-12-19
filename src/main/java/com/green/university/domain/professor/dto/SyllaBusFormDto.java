package com.green.university.domain.professor.dto;

import lombok.Data;
/**
 * 
 * @author 박성희
 *
 */
@Data
public class SyllaBusFormDto { // 강의 계획서 업데이트용 dto

	private Long subjectId;
	private String overview;
	private String objective;
	private String textbook;
	private String program;
	
}
