package com.green.university.entity;

import com.green.university.entity.Student;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PreStuSub {
    // 복합키 방식 사용하지 않는 대신, 이 엔티티도 기존에 저희가 사용하던 방법대로 변경했습니다!!
    // @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Student 엔티티
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;
    // Subject 엔티티
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
}
