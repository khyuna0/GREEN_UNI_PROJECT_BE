package com.green.university.dto.response;

import lombok.Data;

import java.sql.Date;
import java.time.LocalDate;

/**
 * 학생 정보 조회 페이지에
 * 학적 변동 정보 담을 dto
 * @author 김지현
 */
@Data
public class StatusFluctuationInfoDto {

	// 학생 상태 구분
	private String status;
	private LocalDate fromDate;
	private LocalDate toDate;
	private Long studentGrade;
	private Long fromYear;
	private Long fromSemester;
	private Long toYear;
	private Long toSemester;
	private String type;
	private LocalDate appDate;
	private String adoptStatus;
}
