package com.green.university.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Data
@Entity
public class User {

    @Id
    private Long id;
    private String password;
    private String userRole;
}
