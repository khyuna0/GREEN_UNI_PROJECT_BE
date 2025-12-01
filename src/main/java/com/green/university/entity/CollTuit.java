package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CollTuit {
//    @Id
//    @OneToOne(cascade = CascadeType.ALL)
//    private College college;
//    // college id와 외래키

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // CollTuit PK

    @ManyToOne
    @JoinColumn(name = "college_id", nullable = false, unique = true)
    private College college;   // 어느 단과대인지

    private Long amount;       // 등록금 금액

}
