package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.time.LocalDate;

@Data
@Entity
public class StuStat { // 학생 재학 상태 (입학날짜, 휴학 여부 ..)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 학생 한 명이 여러 상태를 가질 수 있음
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private String status;
    private LocalDate fromDate;
    private LocalDate toDate;

    // 한 휴학 신청이 여러 상태로 기록될 수 있음
    @ManyToOne
    @JoinColumn(name = "break_app_id")
    private BreakApp breakApp;

}