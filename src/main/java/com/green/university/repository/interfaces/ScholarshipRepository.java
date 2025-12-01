package com.green.university.repository.interfaces;

import com.green.university.repository.model.Scholarship;
import com.green.university.repository.model.StuSch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author 서영
 *
 */


public interface ScholarshipRepository extends JpaRepository<Scholarship,Long> {

	// 학생의 특정 학기 장학금 유형에 따른 최대 지원 금액
	public Scholarship selectByStudentIdAndSemester(@Param("studentId") Long studentId, @Param("schYear") Long schYear, @Param("semester") Long semester);
	
	// 학생의 이번 학기 장학금 유형 결정
	public Long insertCurrentSchType(StuSch stuSch);
	
}
