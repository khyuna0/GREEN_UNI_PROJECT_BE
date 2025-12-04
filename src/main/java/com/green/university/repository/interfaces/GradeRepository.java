package com.green.university.repository.interfaces;

import com.green.university.entity.Grade;
import com.green.university.entity.StuSub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    Grade findByGrade(String grade);
}
