package com.green.university.dto.response;

import com.green.university.entity.Subject;
import lombok.Data;

/**
 * @author 서영
 * 강의 시간표 조회 시 사용할 Dto
 * 단과대학, 학과 등을 id 대신 이름으로 쉽게 보여주기 위해 이름을 추가해서 만듦
 */
@Data
public class SubjectDto {

	// 단과대 이름
	private String collName;
	
	// 학과 id
	private Long deptId;
	
	// 학과 이름
	private String deptName;
	
	// 과목 id
	private Long id;
	
	// 과목 이름
	private String name;
	
	// 교수 id
	private Long professorId;
	
	// 교수 이름
	private String professorName;
	
	// 강의실 id (==이름)
	private String roomId;
	
	// 강의 구분 (전공/교양)
	private String type;
	
	// 개설 연도
	private Long subYear;
	
	// 개설 학기
	private Long semester;
	
	// 요일
	private String subDay;
	
	// 강의 시작 시간
	private Long startTime;
	
	// 강의 종료 시간
	private Long endTime;
	
	// 이수 가능 학점
	private Long grades;
	
	// 정원
	private Long capacity;
	
	// 현재 인원
	private Long numOfStudent;
	
	// 신청 여부
	private Boolean status;

	// Subject 엔티티 -> SubjectDto로 변환
	public static SubjectDto fromEntity(Subject subject) {
		SubjectDto dto = new SubjectDto();
		dto.setId(subject.getId());
		dto.setName(subject.getName());
		if(subject.getProfessor() != null) {
			dto.setProfessorId(subject.getProfessor().getId());
			dto.setProfessorName(subject.getProfessor().getName());
		}

		if(subject.getRoom() != null) {
			dto.setRoomId(subject.getRoom().getId());
		}

		if(subject.getDepartment() != null) {
			dto.setDeptId(subject.getDepartment().getId());
			dto.setDeptName(subject.getDepartment().getName());
		}
		dto.setType(subject.getType());
		dto.setSubYear(subject.getSubYear());
		dto.setSemester(subject.getSemester());
		dto.setSubDay(subject.getSubDay());
		dto.setStartTime(subject.getStartTime());
		dto.setEndTime(subject.getEndTime());
		dto.setGrades(subject.getGrades());
		dto.setCapacity(subject.getCapacity());
		dto.setNumOfStudent(subject.getNumOfStudent());
		dto.setStatus(false);
        return dto;
    };

}
