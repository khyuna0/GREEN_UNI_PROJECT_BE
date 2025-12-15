package com.green.university.domain.grade.repository;

import com.green.university.domain.grade.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    Optional<Grade> findByGrade(String grade);
}
