package com.green.university.repository.interfaces;

import com.green.university.repository.model.CollTuit;
import com.green.university.repository.model.Tuition;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author 서영
 *
 */


public interface TuitionRepository extends CrudRepository<Tuition,Long> {

	// 특정 학생의 등록금 내역 조회
	public List<Tuition> selectByStudentId(Long studentId);
	
	// 특정 학생의 납부 여부에 따른 등록금 내역 조회
	public List<Tuition> selectByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") Boolean status);
	
	// 학생의 학과-단과대를 기준으로 등록금액 조회
	public CollTuit selectTuiAmountByStudentId(Long studentId);
	
	// 등록금 고지서 생성
	public Long insert(Tuition tuition);

	// 등록금 고지서 생성 여부 확인
	public Tuition selectByStudentIdAndSemester(@Param("studentId") Long studentId, @Param("tuiYear") Long tuiYear, @Param("semester") Long semester);
	
	// 등록금 납부
	public Long updateStatus(@Param("studentId") Long studentId, @Param("tuiYear") Long tuiYear, @Param("semester") Long semester);
	
}