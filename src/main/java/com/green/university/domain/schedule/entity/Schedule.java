package com.green.university.domain.schedule.entity;


import com.green.university.domain.admin.entity.Staff;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

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
