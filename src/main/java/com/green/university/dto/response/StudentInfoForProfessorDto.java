package com.green.university.dto.response;

import com.green.university.entity.StuSub;
import com.green.university.entity.Student;
import lombok.Data;

/**
 * 과목에 대한 학생의 디테일한 정보(교수 조회, 입력용)
 * @author 김지현
 */
@Data
public class StudentInfoForProfessorDto {

	private Long id;
	private Long studentId;
	private String studentName;
	// 학생 소속
	private String deptName;
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

	// StuSub 엔티티 -> StudentInfoForProfessorDto로 변환
	public static StudentInfoForProfessorDto fromEntity (StuSub stuSub) {
		StudentInfoForProfessorDto  dto = new StudentInfoForProfessorDto();
		Student student = stuSub.getStudent();
		dto.setId(stuSub.getId());
		dto.setStudentId(stuSub.getStudent().getId());
		dto.setStudentName(stuSub.getStudent().getName());
		dto.setDeptName(student.getDepartment().getName());
		return dto;
	}
	
}
