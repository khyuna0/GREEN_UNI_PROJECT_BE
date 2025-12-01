package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class StuSubDetail {

    //id
    @Id
    @ManyToOne
    @JoinColumn(name = "stu_sub_id")
    private StuSub stuSub;

    //studentId
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    //subjectId
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private Long absent;
    private Long lateness;
    private Long homework;
    private Long mildExam;
    private Long finalExam;
    private Long convertedMark;

}
