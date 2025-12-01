package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Getter
@Setter
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Date birthDate;
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
