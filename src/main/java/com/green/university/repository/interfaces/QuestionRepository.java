package com.green.university.repository.interfaces;

import com.green.university.dto.response.QuestionDto;
import org.springframework.data.jpa.repository.JpaRepository;


public interface QuestionRepository extends JpaRepository<QuestionDto, Long> {

}
