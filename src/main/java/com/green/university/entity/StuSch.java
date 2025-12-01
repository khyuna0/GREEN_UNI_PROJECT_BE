package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
public class StuSch { // 학적상태 저장 테이블

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Student 엔티티
//    private Student student;

    private Long schYear;
    private Long semester;

//     Scholarship 엔티티
    @ManyToOne
    private Scholarship scholarship;
}
