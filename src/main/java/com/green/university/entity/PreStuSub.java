package com.green.university.entity;

import com.green.university.entity.Student;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PreStuSub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
}
