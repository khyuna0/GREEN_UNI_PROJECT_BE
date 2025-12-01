package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
public class StuSch { // 학적상태 저장 테이블

    @Id
    @Column
    private Long studentId; // 복합키 테이블이라 아이디 정보 저장함

    @Id
    @Column
    private Long schYear;

    @Id
    @Column
    private Long semester;

    @ManyToOne
    @JoinColumn
    private Scholarship schType;
}
