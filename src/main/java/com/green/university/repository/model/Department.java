package com.green.university.repository.model;

import lombok.Data;

@Data
public class Department {

	private Long id;
	private String name;
	private Long collegeId;
}
