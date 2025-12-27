package com.green.university.domain.breakapp.entity;

import com.green.university.domain.student.entity.Student;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * @author 서영
 * 휴학 신청 내역
 */
@Entity
@Data
public class BreakApp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private Long studentGrade;

    private Long fromYear; // 휴학 시작 하기 원하는 연도
    private Long fromSemester;
    private Long toYear; // 휴학 끝내기 원하는 연도
    private Long toSemester;

    private String type; // 휴학 이유
    private String status; // 휴학 처리 상태
    private LocalDate appDate; // 휴학 신청서를 쓴 날짜


}
