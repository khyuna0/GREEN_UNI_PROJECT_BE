package com.green.university.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class StuSubDetail {

    //id
    private StuSub stuSub;

    //studentId
    private Student student;

    //subjectId
    private Subject subject;

    private Long absent;
    private Long lateness;
    private Long homework;
    private Long mildExam;
    private Long finalExam;
    private Long convertedMark;

}
