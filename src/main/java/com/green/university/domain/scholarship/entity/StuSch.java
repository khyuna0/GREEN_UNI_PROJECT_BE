package com.green.university.domain.scholarship.entity;

import com.green.university.domain.student.entity.Student;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class StuSch { // 학생 장학금

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private Long schYear;
    private Long semester;

    @ManyToOne
    private Scholarship schType;
}