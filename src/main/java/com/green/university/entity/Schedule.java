package com.green.university.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;

    private LocalDate startDay;
    private LocalDate endDay;
    private String information;
}
