package com.green.university.domain.scholarship.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Scholarship {

    @Id
    private Long type;

    private Long maxAmount;


}
