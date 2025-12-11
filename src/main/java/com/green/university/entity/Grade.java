package com.green.university.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Grade {

    @Id
    private String grade; // 등급 (A, B, C)

    private Double gradeValue; // 학점 (4.5, 3.0)
}
