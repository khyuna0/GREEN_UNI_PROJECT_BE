package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "colleage_id") // 오타 수정할 것!
    private College colleage;
}
