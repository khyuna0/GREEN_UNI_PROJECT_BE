package com.green.university.entity;

import jakarta.persistence.*;
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
    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    // roomId
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    // deptId
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

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
