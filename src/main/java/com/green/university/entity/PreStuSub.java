package com.green.university.entity;

import com.green.university.entity.Student;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class PreStuSub {

    // Student 엔티티
    @Id
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
    // Subject 엔티티
    @Id
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
}
