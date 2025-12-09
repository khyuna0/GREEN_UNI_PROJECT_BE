package com.green.university.repository;

import com.green.university.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;


public interface QuestionRepository extends JpaRepository<Question, Long> {

}
