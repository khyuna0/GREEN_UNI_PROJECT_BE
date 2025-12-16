package com.green.university.domain.counseling.dto;

import com.green.university.domain.counseling.entity.CounselingSchedule;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CounselingInfoDto {

    private Long id;

    private Long professorId; // 담당 교수 아이디
    
    private String professorName; // 담당 교수 이름

    private String deptName; // 담당 교수 학과

    private Long subYear; // 년도

    private Long semester; // 학기

    private LocalDate counselingDate;

    private String dayOfWeek; // 요일

    private Long startTime; // 시작 시간

    private Long endTime; // 종료 시간

    public CounselingInfoDto(CounselingSchedule c) {
        this.id = c.getId();
        this.professorId = c.getProfessor().getId();
        this.professorName = c.getProfessor().getName();
        this.deptName = c.getProfessor().getDepartment().getName();
        this.subYear = c.getSubYear();
        this.semester = c.getSemester();
        this.counselingDate = c.getCounselingDate();
        this.dayOfWeek = c.getDayOfWeek();
        this.startTime = c.getStartTime();
        this.endTime = c.getEndTime();
    }
}


