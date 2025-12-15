package com.green.university.domain.student.entity;

import com.green.university.domain.university.entity.Department;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

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

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private Long grade;
    private Long semester;
    private LocalDate entranceDate;
    private LocalDate graduationDate;


}
