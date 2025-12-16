package com.green.university.domain.professor.dto;

import com.green.university.domain.professor.entity.Professor;
import com.green.university.domain.professor.entity.Syllabus;
import com.green.university.domain.subject.entity.Subject;
import lombok.Data;

/**
 * 강의계획서 조회용 dto
 * @author 김지현
 */
@Data
public class ReadSyllabusDto {

    // subject
	private Long subjectId;
	private String name;
	private Long subYear;
	private Long semester;
	// 학점
	private Long credits;
	private String type;
	// 요일
	private String subDay;
	private Long startTime;
	private Long endTime;
	private String roomId;
	private String collegeName;
    private String deptName;

    // professor
    private Long professorId;
	private String professorName;
	private String tel;
	private String email;

    // syllabus 부분
	private String overview;
	private String objective;
	private String textbook;
	private String program;

    public ReadSyllabusDto(Subject s, Professor p, Syllabus sy) {

        // subject 부분
        this.subjectId = s.getId();
        this.name = s.getName();
        this.subYear = s.getSubYear();
        this.semester = s.getSemester();
        this.credits = s.getCredits();
        this.type = s.getType();
        this.subDay = s.getSubDay();
        this.startTime = s.getStartTime();
        this.endTime = s.getEndTime();
        this.roomId = s.getRoom().getId();
        this.collegeName = s.getDepartment().getCollege().getName();
        this.deptName = s.getDepartment().getName();

        // professor
        this.professorId = p.getId();
        this.professorName = p.getName();
        this.tel = p.getTel();
        this.email = p.getEmail();

        // syllabus
        this.overview = sy.getOverview();
        this.objective = sy.getObjective();
        this.textbook = sy.getTextbook();
        this.program = sy.getProgram();


    }
}
