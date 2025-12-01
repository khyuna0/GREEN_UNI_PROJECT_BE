package com.green.university.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Data
@Entity
public class Tuition {

    //studentId
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @Id
    private Long tuiYear;

    @Id
    private Long semester;

    private Long tuiAmount;

    //schType
    //private Scholarship scholarship;

    private Long schAmount;
    private Long status;



}
