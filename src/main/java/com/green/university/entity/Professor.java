package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Data
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Date birthDate;

    private String gender;

    private String address;
    private String tel;
    private String email;

//    Department 엔티티
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private Date hireDate;
}
