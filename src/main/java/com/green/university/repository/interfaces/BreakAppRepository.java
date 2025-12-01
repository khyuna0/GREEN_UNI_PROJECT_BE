package com.green.university.repository.interfaces;

import com.green.university.dto.BreakAppFormDto;
import com.green.university.entity.BreakApp;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author 서영
 *
 */

public interface BreakAppRepository extends JpaRepository<BreakApp,Long> {

	// 휴학 신청하기
	public Long insert(BreakAppFormDto breakAppFormDto);
	
	// 학생의 휴학 신청 조회하기
	public List<BreakApp> selectByStudentId(Long studentId);
	
	// 처리되지 않은 휴학 신청 조회하기 (교직원용)
	public List<BreakApp> selectByStatus(String status);
	
	// 특정 휴학 신청서 조회하기
	public BreakApp selectById(Long id);
	
	// 휴학 신청 취소하기 (학생용)
	
	// 휴학 신청 처리하기 (교직원용)
	public Long updateById(@Param("id") Long id, @Param("status") String status);
	
}
