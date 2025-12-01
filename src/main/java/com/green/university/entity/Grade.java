package com.green.university.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Grade {

    @Id
    private String grade;

    // 학점
    private Long gradeValue;
}
