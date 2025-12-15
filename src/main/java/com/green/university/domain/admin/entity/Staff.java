package com.green.university.domain.admin.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;


import java.time.LocalDate;

@Entity
@Data
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate birthDate;
    private String gender;
    private String address;
    private String tel;
    private String email;
    private LocalDate hireDate;

}
