package com.green.university.domain.grade.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Grade {

    @Id
    private String letterGrade; // 등급 (A, B, C)

    private Double gradePoint; // 학점 (4.5, 3.0)
}
