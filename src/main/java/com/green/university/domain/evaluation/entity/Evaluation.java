package com.green.university.domain.evaluation.entity;

import com.green.university.domain.evaluation.dto.EvaluationFormDto;
import com.green.university.domain.student.entity.Student;
import com.green.university.domain.subject.entity.Subject;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
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

    // 생성자
    public Evaluation(Student student, Subject subject, EvaluationFormDto dto) {
        this.student = student;
        this.subject = subject;
        this.answer1 = dto.getAnswer1();
        this.answer2 = dto.getAnswer2();
        this.answer3 = dto.getAnswer3();
        this.answer4 = dto.getAnswer4();
        this.answer5 = dto.getAnswer5();
        this.answer6 = dto.getAnswer6();
        this.answer7 = dto.getAnswer7();
        this.improvements = dto.getImprovements();
    }
}
