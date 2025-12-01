package com.green.university.repository.model;

import lombok.Data;

import java.security.Timestamp;

@Data
public class Notice {

	private Long id;
	private String category;
	private String title;
	private String content;
	private Long views;
	private Timestamp createdTime;
	
	private String uuidFilename;
	private String originFilename;
	
	public String setUpImage() {
		return "/images/uploads/" + uuidFilename;
	} 
}
