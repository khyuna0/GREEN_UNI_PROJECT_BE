package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class CounselingReserve { // 확정된 상담

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 학생
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // 교수 오픈 일정
    @ManyToOne
    @JoinColumn(name = "counselingSchedule_id", nullable = false)
    private CounselingSchedule counselingSchedule;

    // 화상 상담 방 코드
    @Column(unique = true)
    private String roomCode;

    // 학생 위험 상태 - 위험학생 아니면 null
    @ManyToOne
    @JoinColumn(name = "dropout_risk_id", nullable = true)
    private DropoutRisk dropoutRisk;

    // 상담 사유
    @Column(length = 200)
    private String reason;

}
