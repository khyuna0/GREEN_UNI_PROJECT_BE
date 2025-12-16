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

    // Student 엔티티 (studentId)
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private Long studentGrade;

    private Long fromYear;

    private Long fromSemester;

    private Long toYear;

    private Long toSemester;

    private String type;

    // 날짜 .. 유틸 ..
    private LocalDate appDate;
    private String status;

}
