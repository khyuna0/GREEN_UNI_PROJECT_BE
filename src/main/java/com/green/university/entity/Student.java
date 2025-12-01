package com.green.university.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    //private Department department;

    private Long grade;
    private Long semester;
    private LocalDate entranceDate;
    private LocalDate graduationDate;


}
