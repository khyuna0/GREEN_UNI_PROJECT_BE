package com.green.university.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Data
@Entity
public class Tuition {

    //studentId
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
