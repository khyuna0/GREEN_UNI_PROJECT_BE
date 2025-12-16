package com.green.university.domain.subject.entity;

import com.green.university.domain.grade.entity.Grade;
import com.green.university.domain.student.entity.Student;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class StuSub { // 학생의 수강 과목과 과목에 대한 학점

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
    @JoinColumn(name = "grade_id")
    private Grade letterGrade;
    
    private Long credits; // 해당 과목의 학점
}