package com.green.university.dto.response;

import com.green.university.entity.Professor;
import com.green.university.entity.Subject;
import lombok.Data;

/**
 * @author 서영
 */
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
}

