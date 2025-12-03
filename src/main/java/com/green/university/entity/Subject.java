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

    private String name; // 과목명

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor; // 담당교수

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room; // 강의실

    // deptId
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department; // 학과

    private String type; // 전공 or 교양

    private Long subYear;
    private Long semester;
    private String subDay;
    private Long startTime;
    private Long endTime;
    private Long grades;
    private Long capacity;
    private Long numOfStudent;


}
