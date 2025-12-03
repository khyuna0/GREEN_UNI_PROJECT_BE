package com.green.university.repository.interfaces;

import com.green.university.dto.UpdateStudentGradeDto;

import com.green.university.entity.StuSub;
import com.green.university.entity.StuSubDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * stu_sub_detail_tb DAO
 * @author 김지현
 *
 */

public interface StuSubDetailRepository extends JpaRepository<StuSubDetail,Long> {
	
	// 학생 성적 업데이트
	Long updateGrade(UpdateStudentGradeDto updateStudentGradeDto);

	/**
	 * @author 서영
	 */
	Long insert(@Param("id") Long id, @Param("studentId") Long studentId, @Param("subjectId") Long subjectId);

	Optional<StuSubDetail> findByStuSub(StuSub stuSub);
}
