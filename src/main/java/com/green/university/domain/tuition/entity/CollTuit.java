package com.green.university.domain.tuition.entity;

import com.green.university.domain.university.entity.College;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CollTuit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "college_id", nullable = false, unique = true)
    private College college;   // 어느 단과대인지

    private Long amount;       // 등록금 금액

}
