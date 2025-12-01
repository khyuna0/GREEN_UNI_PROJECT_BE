package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;

@Data
@Entity
public class StuStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //studentId
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private String status;
    private Date fromDate;
    private Date toDate;

    //breakAppId
    @ManyToOne
    @JoinColumn(name = "break_app_id")
    private BreakApp breakApp;

}