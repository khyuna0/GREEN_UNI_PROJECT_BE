package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class StuSub { // 학생의 과목과 학점

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "grade_grade")
    private Grade grade;

    private Long completeGrade; // 최종학점?
}