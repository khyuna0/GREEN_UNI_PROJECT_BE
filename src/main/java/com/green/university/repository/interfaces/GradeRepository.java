package com.green.university.repository.interfaces;

import com.green.university.entity.Grade;
import com.green.university.entity.StuSub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    Optional<Grade> findByGrade(String grade);
}
