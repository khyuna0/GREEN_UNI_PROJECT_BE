package com.green.university.domain.counseling.entity;

import com.green.university.domain.student.entity.Student;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.domain.dropoutrisk.entity.DropoutRisk;
import jakarta.persistence.*;
import lombok.Data;

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

    // 상담 요청한 과목
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

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
