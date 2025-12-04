package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class SyllaBus { // 강의 계획서

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    private String overview; // 강의 개요
    private String objective; // 강의 목표
    private String textbook; // 교재 정보
    private String program; // 주간 계획?


}
