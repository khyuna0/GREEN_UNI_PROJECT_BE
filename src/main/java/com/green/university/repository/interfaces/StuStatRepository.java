package com.green.university.repository.interfaces;

import com.green.university.dto.response.StudentInfoStatListDto;
import com.green.university.entity.StuStat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author 서영
 * 학적 변동
 */

public interface StuStatRepository extends JpaRepository<StuStat,Long> {

	// 해당 학생의 모든 학적 변동 내역 조회
	List<StuStat> findAllByStudentIdOrderByIdDesc(Long studentId);

	/**
	 * 학생 내정보 조회에 학적변동리스트
	 * @author 김지현
	 */
	// 이건 웹에서 직접 확인 해보고 고칠 것
	public List<StudentInfoStatListDto> selectStuStatListBystudentId(Long studentId);
	
}
