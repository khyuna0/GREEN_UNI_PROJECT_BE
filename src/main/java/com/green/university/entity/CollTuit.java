package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CollTuit {
    @Id
    @OneToOne(cascade = CascadeType.ALL)
    private College college;
    // college id와 외래키 ..

    private Long amount;
}
