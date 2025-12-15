package com.green.university.domain.evaluation.entity;

import com.green.university.domain.student.entity.Student;
import com.green.university.domain.subject.entity.Subject;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Evaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

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
