package com.green.university.dto;

import lombok.Data;

/**
 * 학생 출결 및 성적 기입 폼 stu_sub_detail_tb update용
 * 
 * @author 김지현
 */
@Data
public class UpdateStudentGradeDto {
	
	private Long studentId;
	private Long subjectId;

	// 결석 횟수
	private Long absent;
	// 지각 횟수
	private Long lateness;
	// 과제 점수
	private Long homework;
	// 중간고사 점수
	private Long midExam;
	// 기말고사 점수
	private Long finalExam;
	// 총합 환산 점수
	private Long convertedMark;
	// 등급
	private String grade;

}
