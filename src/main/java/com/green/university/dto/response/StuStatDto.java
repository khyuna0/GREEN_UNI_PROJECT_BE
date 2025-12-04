package com.green.university.dto.response;

import com.green.university.entity.BreakApp;
import com.green.university.entity.StuStat;
import com.green.university.entity.Student;
import lombok.Data;

import java.time.LocalDate;
@Data
public class StuStatDto {

    private Long id;
    private Student student;
    private String status;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BreakApp breakApp;

    public StuStatDto(StuStat s) {
        this.id = s.getId();
        this.student = s.getStudent();
        this.status = s.getStatus();
        this.fromDate = s.getFromDate();
        this.toDate = s.getToDate();
        this.breakApp = s.getBreakApp();
    }

}
