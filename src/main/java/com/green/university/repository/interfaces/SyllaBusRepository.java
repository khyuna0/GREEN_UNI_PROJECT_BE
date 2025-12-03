package com.green.university.repository.interfaces;

import com.green.university.entity.SyllaBus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SyllaBusRepository extends JpaRepository<SyllaBus, Long> {

    // 과목명으로 강의계획서 찾기
    Optional<SyllaBus> findBySubject_Id(Long subjectId);
}
