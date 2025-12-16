package com.green.university.domain.subject.entity;

import com.green.university.domain.professor.entity.Professor;
import com.green.university.domain.university.entity.Department;
import com.green.university.domain.university.entity.Room;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
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
    private Long credits; // 과목 당 학점
    private Long capacity; // 정원
    private Long numOfStudent;


}
