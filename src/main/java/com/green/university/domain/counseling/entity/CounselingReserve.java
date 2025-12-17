package com.green.university.domain.counseling.entity;

import com.green.university.domain.student.entity.Student;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.domain.dropoutrisk.entity.DropoutRisk;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounselingReserve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상담 신청 학생
    @ManyToOne(fetch = FetchType.LAZY)
    private Student student;

    // 신청 과목
    @ManyToOne(fetch = FetchType.LAZY)
    private Subject subject;

    // 교수 상담 일정 (시간 슬롯)
    @ManyToOne(fetch = FetchType.LAZY)
    private CounselingSchedule counselingSchedule;

    // 상담 사유
    @Column(length = 500)
    private String reason;

    // 승인 상태
    @Enumerated(EnumType.STRING)
    private ApprovalState approvalState;

    // 승인 시 생성되는 화상 상담 방 코드
    private String roomCode;

    // 위험 학생인 경우 연결
    @ManyToOne(fetch = FetchType.LAZY)
    private DropoutRisk dropoutRisk;
}

