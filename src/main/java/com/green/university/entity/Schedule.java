package com.green.university.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;

    private LocalDate startDay; // 시작 날짜
    private LocalDate endDay; // 종료 날짜
    private String information; // 학사 일정 내용
}
