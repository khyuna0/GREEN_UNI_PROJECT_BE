package com.green.university.repository.model;

import com.green.university.utils.DateUtil;
import lombok.Data;

import java.sql.Date;

/**
 * @author 서영
 * 휴학 신청 내역
 */
@Data
public class BreakApp {

	private Long id;
	private Long studentId;
	private Long studentGrade;
	private Long fromYear;
	private Long fromSemester;
	private Long toYear;
	private Long toSemester;
	private String type;
	private Date appDate;
	private String status;
	
	public String appDateFormat() {
		return DateUtil.dateFormat(appDate);
	}
	
}
