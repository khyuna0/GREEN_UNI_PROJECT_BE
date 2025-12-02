package com.green.university.entity;

import com.green.university.dto.CreateStudentDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDate birthDate;
    private String gender;
    private String address;
    private String tel;
    private String email;

    //deptId
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private Long grade;
    private Long semester;
    private LocalDate entranceDate;
    private LocalDate graduationDate;


}
