package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class CounselingPreReserve { // 상담 신청(예비) 1주 마다 초기화


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student; // 대상 학생

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신청 학생
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // 대상 교수
    @ManyToOne
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    // 교수 오픈 일정
    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private CounselingSchedule counselingSchedule;

    // 신청 주차 기준
    private LocalDate weekStartDate; // 해당 주 월요일

    // 상담 사유
    @Column(length = 200)
    private String reason;

    // 상태
    @Enumerated(EnumType.STRING)
    private RequestStatus status; // REQUESTED / APPROVED / REJECTED

    private LocalDateTime createdAt;
}


