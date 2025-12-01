package com.green.university.repository.interfaces;

import com.green.university.dto.response.StuSubAppDto;
import com.green.university.dto.response.StuSubDayTimeDto;
import com.green.university.dto.response.StuSubSumGradesDto;
import com.green.university.entity.PreStuSub;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author 서영
 *
 */

public interface PreStuSubRepository extends JpaRepository<PreStuSub,Long> {

	// 학생의 예비 수강 신청 내역에 해당 강의가 있는지 조회
	PreStuSub findByStudentIdAndSubjectId(Long studentId, Long subjectId);
	
	// 학생의 이번 학기 전체 예비 수강 신청 내역 조회
	List<StuSubAppDto> findByStudentIdAndSemester(@Param("studentId") Long studentId, @Param("subYear") Long subYear, @Param("semester") Long semester);
	
//	// 학생의 예비 수강 신청 학점 조회 (보류
//	StuSubSumGradesDto selectSumGrades(@Param("studentId") Long studentId, @Param("subYear") Long subYear, @Param("semester") Long semester);
//

//	// 학생의 예비 수강 신청 내역 시간표 조회
//	List<StuSubDayTimeDto> selectDayTime(Long studentId);
//
//	// 예비 수강 신청 내역 추가
//	Long insert(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);
//
//	// 예비 수강 신청 내역 삭제
//	Long delete(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);


	// 예비 수강 신청 내역에 해당 강의가 있는 학생들 조회
	List<PreStuSub> selectBySubjectId(Long subjectId);
	
}
