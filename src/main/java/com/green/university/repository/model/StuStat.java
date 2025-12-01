package com.green.university.repository.model;

import lombok.Data;

import java.sql.Date;

@Data
public class StuStat {

	private Long id;
	private Long studentId;
	private String status;
	private Date fromDate;
	private Date toDate;
	
}
