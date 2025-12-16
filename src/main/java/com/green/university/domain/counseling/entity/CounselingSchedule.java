package com.green.university.domain.counseling.entity;

import com.green.university.domain.professor.entity.Professor;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class CounselingSchedule { // 교수가 열어주는 상담 일정

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor; // 담당 교수

    private Long subYear; // 년도

    private Long semester; // 학기

    private LocalDate counselingDate;

    private String dayOfWeek; // 요일

    private Long startTime; // 시작 시간

    private Long endTime; // 종료 시간

    private boolean reserved = false; // 예약 여부 매칭되면 true
}
