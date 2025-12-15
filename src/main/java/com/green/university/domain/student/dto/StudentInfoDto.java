package com.green.university.domain.student.dto;

import com.green.university.domain.student.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentInfoDto {

	private Long id;
	private String name;
	private LocalDate birthDate;
	private String gender;
	private String address;
	private String tel;
	private String email;
	private Long deptId;
	private Long grade;
	private Long semester;
	private LocalDate entranceDate;
	private LocalDate graduationDate;
	private String deptName;
	private String collegeName;

	// Student Entity -> StudentInfoDto DTO 변환
	public static StudentInfoDto fromEntity(Student student) {
		StudentInfoDto dto = new StudentInfoDto();
		dto.setId(student.getId());
		dto.setName(student.getName());
		dto.setBirthDate(student.getBirthDate());
		dto.setGender(student.getGender());
		dto.setAddress(student.getAddress());
		dto.setTel(student.getTel());
		dto.setEmail(student.getEmail());
		// departmentName은 연관 엔티티에서 꺼내오기
		if (student.getDepartment() != null) {
			dto.setDeptId(student.getDepartment().getId());
			dto.setDeptName(student.getDepartment().getName());
		}
		dto.setGrade(student.getGrade());
		dto.setSemester(student.getSemester());
		dto.setEntranceDate(student.getEntranceDate());
		dto.setGraduationDate(student.getGraduationDate());
		return dto;
	}
}
