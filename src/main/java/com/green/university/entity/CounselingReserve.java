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

    // 교수
    @ManyToOne
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    // 교수 오픈 일정
    @ManyToOne
    @JoinColumn(name = "counselingSchedule_id", nullable = false)
    private CounselingSchedule counselingSchedule;

    // 화상 상담 방 코드
    @Column(unique = true)
    private String roomCode;

    // 학생 위험 상태
    @Enumerated(EnumType.STRING)
    private RiskLevel risklevel; // danger, warning

    @Enumerated(EnumType.STRING)
    private RiskStatus status = RiskStatus.DETECTED; // DETECTED 포착됨, CONSULT_REQ 상담예약, RESOLVED 해결완료

    // 상담 사유
    @Column(length = 200)
    private String reason;

}
