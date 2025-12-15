package com.green.university.domain.tuition.entity;

import com.green.university.domain.scholarship.entity.Scholarship;
import com.green.university.domain.student.entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Tuition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기존 복합 키 테이블을 기존 방식으로 수정함

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private Long tuiYear; // 등록 연도

    private Long semester; // 등록 학기

    private Long tuiAmount; // 등록 금액

    private Long payAmount; // 실납부금액 / 없어서 추가함! (수정)

    @ManyToOne
    @JoinColumn(name = "sch_type")
    private Scholarship schType;

    private Long schAmount; // 장학 금액

    private boolean status = false; // 납부 여부, 기본값 false인듯 (수정)

    // 등록금 고지서 생성을 위한 생성자
    public Tuition(Student student,
                   Long tuiYear,
                   Long semester,
                   Long tuiAmount,
                   Long payAmount,
                   Scholarship schType,
                   Long schAmount) {

        this.student = student;
        this.tuiYear = tuiYear;
        this.semester = semester;
        this.tuiAmount = tuiAmount;
        this.payAmount = payAmount;
        this.schType = schType;
        this.schAmount = schAmount;
        this.status = false; // 기본값
    }

}
