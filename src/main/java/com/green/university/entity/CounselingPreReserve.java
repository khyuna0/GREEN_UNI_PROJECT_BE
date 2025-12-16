package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;

import javax.annotation.Nullable;

@Entity
@Data
public class CounselingPreReserve { // 상담 신청(예비) 1주 마다 초기화

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student; // 대상 학생

    // 교수 오픈 일정 (여기에 교수 정보 저장되어 있음)
    @ManyToOne
    @JoinColumn(name = "counselingSchedule_id", nullable = false)
    private CounselingSchedule counselingSchedule;

    // 상담 요청한 과목
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    // 상담 사유
    @Column(length = 200)
    private String reason;

    // 학생 위험 상태 조인/위험학생 아니면 null
    @ManyToOne
    @JoinColumn(name = "dropout_risk_id", nullable = true)
    private DropoutRisk dropoutRisk;

    // 승인 여부
    @Enumerated(EnumType.STRING)
    private  ReserveStatus approvalState = ReserveStatus.REQUESTED;
    // 학생 신청, 교수 승인(승인 시 예약 생성), 반려
}


