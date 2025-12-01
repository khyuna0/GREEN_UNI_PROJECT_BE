package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;

@Data
@Entity
public class StuStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //studentId
    private Student student;

    private String status;
    private Date fromDate;
    private Date toDate;

    //breakAppId
    //private BreakApp breakApp;

}