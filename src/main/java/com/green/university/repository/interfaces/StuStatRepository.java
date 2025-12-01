package com.green.university.repository.interfaces;

import com.green.university.dto.response.StudentInfoStatListDto;
import com.green.university.repository.model.StuStat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author 서영
 * 학적 변동
 */

public interface StuStatRepository extends JpaRepository<StuStat,Long> {

	// 해당 학생의 모든 학적 변동 내역 조회
	public List<StuStat> selectByStudentIdOrderbyIdDesc(Long studentId);
	
	// 학생의 학적 상태 생성
	public Long insert(@Param("studentId") Long studentId, @Param("status") String status, @Param("toDate") String toDate, @Param("breakAppId") Long breakAppId);

	// 학생의 기존 학적 상태의 to_date를 now()로 변경 
	public Long updateOldStatus(Long id);
	
	/**
	 * 학생 내정보 조회에 학적변동리스트
	 * @author 김지현
	 */
	public List<StudentInfoStatListDto> selectStuStatListBystudentId(Long studentId);
	
}
