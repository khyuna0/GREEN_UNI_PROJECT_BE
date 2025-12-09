package com.green.university.service;

import com.green.university.dto.response.QuestionDto;
import com.green.university.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionService {

	@Autowired
	private QuestionRepository questionRepository;

	@Transactional
	public QuestionDto readQuestion() {
		QuestionDto dto = (QuestionDto) questionRepository.findAll();
		return dto;
	}
}
