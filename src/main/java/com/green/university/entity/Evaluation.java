package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Student 엔티티 studentId
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    // Subject 엔티티 subjectId
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private Long answer1;
    private Long answer2;
    private Long answer3;
    private Long answer4;
    private Long answer5;
    private Long answer6;
    private Long answer7;
    private String improvements;
}
