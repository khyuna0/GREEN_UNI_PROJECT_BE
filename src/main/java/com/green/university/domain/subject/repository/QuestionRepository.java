package com.green.university.domain.subject.repository;

import com.green.university.domain.subject.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;


public interface QuestionRepository extends JpaRepository<Question, Long> {

}
