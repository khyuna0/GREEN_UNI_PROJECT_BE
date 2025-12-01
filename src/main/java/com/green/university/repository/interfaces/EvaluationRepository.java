package com.green.university.repository.interfaces;

import com.green.university.dto.EvaluationDto;
import com.green.university.dto.MyEvaluationDto;
import com.green.university.entity.Evaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 
 * @author 편용림
 *
 */

public interface EvaluationRepository extends JpaRepository<Evaluation,Long> {
	
	// 강의 평가 제출 (학생)
	public Long insert(EvaluationDto evaluationFormDto);
	// 강의평가 했는지 조회 (학생)
	public Evaluation selectEvaluation(Long studentId);
	// 강의평가 조회 (교수)
	public List<MyEvaluationDto> selectMyEvaluationDtoByProfessorId(Long professorId);
	// 과목별 강의평가 조회 (교수)
	public List<MyEvaluationDto> selectEvaluationDtoByprofessorIdAndName(@Param("professorId") Long professorId, @Param("name") String Name);
	// 강의평가 과목 조회 (교수)
	public List<MyEvaluationDto> selectEvaluationDto(Long professorId);
}
