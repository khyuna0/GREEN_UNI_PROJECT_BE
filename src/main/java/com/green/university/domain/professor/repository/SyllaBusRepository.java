package com.green.university.domain.professor.repository;

import com.green.university.domain.professor.entity.Syllabus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SyllaBusRepository extends JpaRepository<Syllabus, Long> {

    // 과목명으로 강의계획서 찾기
    Optional<Syllabus> findBySubject_Id(Long subjectId);
}
