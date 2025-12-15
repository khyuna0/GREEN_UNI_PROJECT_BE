package com.green.university.domain.professor.entity;

import com.green.university.domain.university.entity.Department;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Professor {

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
    private LocalDate hireDate;
}
