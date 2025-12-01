package com.green.university.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class SyllaBus {

    //subjectId
    @Id
    private Subject subject;

    private String overview;
    private String objective;
    private String textbook;
    private String program;


}
