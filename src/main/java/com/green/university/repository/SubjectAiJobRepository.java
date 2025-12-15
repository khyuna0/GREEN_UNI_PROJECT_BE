package com.green.university.repository;

import com.green.university.entity.SubjectAiJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectAiJobRepository extends JpaRepository<SubjectAiJob, Long> {
    Optional<SubjectAiJob> findBySubject_Id(Long subjectId);
}

