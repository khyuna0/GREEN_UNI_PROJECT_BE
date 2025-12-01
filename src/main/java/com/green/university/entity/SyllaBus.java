package com.green.university.entity;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class SyllaBus {

    //subjectId
    private Subject subject;

    private String overview;
    private String objective;
    private String textbook;
    private String program;


}
