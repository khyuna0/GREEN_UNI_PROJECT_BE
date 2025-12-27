package com.green.university.domain.admin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class User {

    @Id
    private Long id;
    @JsonIgnore
    private String password;
    private String userRole;
    private String name;
}
