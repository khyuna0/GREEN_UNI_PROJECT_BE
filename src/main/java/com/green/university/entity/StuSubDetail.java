package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class StuSubDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stu_sub_id")
    private StuSub stuSub;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private Long absent;
    private Long lateness;
    private Long homework;
    private Long mildExam;
    private Long finalExam;
    private Double convertedMark; // 환산점수
    private String grade; // 등급 (단순 출력용)

    @Column(nullable = false)
    private boolean finalized = false; // 성적 최종 확정 여부

}
