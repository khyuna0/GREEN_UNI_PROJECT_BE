package com.green.university.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class StuSub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //studentId;
    private Student student;

    //subjectId
    //private Subject subject;

    //grade
    //private Grade grade;

    private Long completeGrade;
}