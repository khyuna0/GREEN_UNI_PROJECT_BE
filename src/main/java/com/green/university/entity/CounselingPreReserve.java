package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CounselingPreReserve { // 상담 신청(예비) 1주 마다 초기화

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student; // 대상 학생

    // 대상 교수
    @ManyToOne
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    // 교수 오픈 일정
    @ManyToOne
    @JoinColumn(name = "counselingSchedule_id", nullable = false)
    private CounselingSchedule counselingSchedule;

    // 상담 사유
    @Column(length = 200)
    private String reason;

    // 학생 위험 상태
    @Enumerated(EnumType.STRING)
    private RiskLevel risklevel; // danger, warning

    @Enumerated(EnumType.STRING)
    private RiskStatus status = RiskStatus.DETECTED; // DETECTED 포착됨, CONSULT_REQ 상담예약, RESOLVED 해결완료

    // 승인 여부
    @Enumerated(EnumType.STRING)
    private  ReserveStatus approvalState = ReserveStatus.REQUESTED;
    // 학생 신청, 교수 승인(승인 시 예약 생성), 반려
}


