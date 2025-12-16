package com.green.university.domain.subject.entity;

import com.green.university.domain.student.entity.Student;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PreStuSub { // 학생 예비 수강 신청

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    // 수강 신청 전환 성공 여부
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean status = true; // 기본값 true (예비 신청 시 성공 상태)
}
