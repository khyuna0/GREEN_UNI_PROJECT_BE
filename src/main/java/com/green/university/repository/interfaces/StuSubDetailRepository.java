package com.green.university.repository.interfaces;

import com.green.university.dto.UpdateStudentGradeDto;

import org.springframework.data.repository.query.Param;

/**
 * stu_sub_detail_tb DAO
 * @author 김지현
 *
 */

public interface StuSubDetailRepository {
	
	// 학생 성적 업데이트
	Long updateGrade(UpdateStudentGradeDto updateStudentGradeDto);

	/**
	 * @author 서영
	 */
	Long insert(@Param("id") Long id, @Param("studentId") Long studentId, @Param("subjectId") Long subjectId);
	
}
