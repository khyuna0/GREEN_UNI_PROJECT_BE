package com.green.university.domain.counseling.dto;

import com.green.university.domain.counseling.entity.CounselingSchedule;
import com.green.university.domain.professor.entity.Professor;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CounselingScheduleDto {

    private Long id;

    private Professor professor; // 담당 교수

    private Long subYear; // 년도

    private Long semester; // 학기

    private LocalDate counselingDate;

    private String dayOfWeek; // 요일

    private Long startTime; // 시작 시간

    private Long endTime; // 종료 시간

    private boolean reserved; // 예약 여부 매칭되면 true

    public CounselingScheduleDto(CounselingSchedule s) {
        this.id = s.getId();
        this.professor = s.getProfessor();
        this.subYear = s.getSubYear();
        this.semester = s.getSemester();
        this.counselingDate = s.getCounselingDate();
        this.dayOfWeek = s.getDayOfWeek();
        this.startTime = s.getStartTime();
        this.endTime = s.getEndTime();
        this.reserved = s.isReserved();
    }

}
