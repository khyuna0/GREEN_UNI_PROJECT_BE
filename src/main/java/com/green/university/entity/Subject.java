package com.green.university.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // professorId
    //private Professor professor;

    // roomId
    //private Room room;

    // deptId
    // private Department department;

    private String type;
    private Long subYear;
    private Long semester;
    private String subDay;
    private Long startTime;
    private Long endTime;
    private Long grades;
    private Long capacity;
    private Long numOfStudent;


}
