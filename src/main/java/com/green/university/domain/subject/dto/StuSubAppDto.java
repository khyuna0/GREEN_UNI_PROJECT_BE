package com.green.university.domain.subject.dto;

import com.green.university.domain.professor.entity.Professor;
import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.entity.Subject;
import lombok.Data;

@Data
public class StuSubAppDto {

    private Long studentId;
    private Long subjectId;
    private String subjectName;
    private String professorName;
    private Long grades;
    private String subDay;
    private Long startTime;
    private Long endTime;
    private Long numOfStudent;
    private Long capacity;
    private String roomId;
    private Boolean status;

    // 생성자 추가
    public StuSubAppDto(Long studentId, Subject s, Professor p) {
        this.studentId = studentId;
        this.subjectId = s.getId();
        this.subjectName = s.getName();
        this.professorName = p.getName();
        this.grades = s.getGrades();
        this.subDay = s.getSubDay();
        this.startTime = s.getStartTime();
        this.endTime = s.getEndTime();
        this.numOfStudent = s.getNumOfStudent();
        this.capacity = s.getCapacity();
        this.roomId = s.getRoom().getId();
        this.status = true;
    }

    public StuSubAppDto(Long studentId, Subject s, Professor p, boolean status) {
        this.studentId = studentId;
        this.subjectId = s.getId();
        this.subjectName = s.getName();
        this.professorName = p.getName();
        this.grades = s.getGrades();
        this.subDay = s.getSubDay();
        this.startTime = s.getStartTime();
        this.endTime = s.getEndTime();
        this.numOfStudent = s.getNumOfStudent();
        this.capacity = s.getCapacity();
        this.roomId = s.getRoom().getId();
        this.status = status;
    }

    public StuSubAppDto() {

    }

    public static StuSubAppDto fromEntity(StuSub stuSub) {
        StuSubAppDto stuSubAppDto = new StuSubAppDto();
        stuSubAppDto.setStudentId(stuSub.getStudent().getId());
        stuSubAppDto.setSubjectId(stuSub.getSubject().getId());
        stuSubAppDto.setSubjectName(stuSub.getSubject().getName());
        stuSubAppDto.setProfessorName(stuSub.getSubject().getProfessor().getName());
        stuSubAppDto.setGrades(stuSub.getSubject().getGrades());
        stuSubAppDto.setSubDay(stuSub.getSubject().getSubDay());
        stuSubAppDto.setStartTime(stuSub.getSubject().getStartTime());
        stuSubAppDto.setEndTime(stuSub.getSubject().getEndTime());
        stuSubAppDto.setNumOfStudent(stuSub.getSubject().getNumOfStudent());
        stuSubAppDto.setCapacity(stuSub.getSubject().getCapacity());
        stuSubAppDto.setRoomId(stuSub.getSubject().getRoom().getId());
        stuSubAppDto.setStatus(true);
        return stuSubAppDto;
    }
}

