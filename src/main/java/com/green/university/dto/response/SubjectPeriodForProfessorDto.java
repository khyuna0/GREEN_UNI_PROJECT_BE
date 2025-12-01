package com.green.university.dto.response;

import lombok.Data;

/**
 * 교수의 과목 학기별 조회를 위한
 * 학기 select dto
 * @author 김지현
 */
@Data
public class SubjectPeriodForProfessorDto {
	
	private Long id;
	private Long subYear;
	private Long semester;

}
