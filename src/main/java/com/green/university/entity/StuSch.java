package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
public class StuSch { // 학적상태 저장 테이블
    
    // Student 엔티티
    @Id
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @Id
    private Long schYear;

    @Id
    private Long semester;

//     Scholarship 엔티티
    @ManyToOne
    @JoinColumn(name = "sch_type")
    private Scholarship schType;
}
