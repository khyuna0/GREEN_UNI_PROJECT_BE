package com.green.university.repository.interfaces;

import com.green.university.dto.response.GradeDto;
import com.green.university.dto.response.GradeForScholarshipDto;
import com.green.university.dto.response.MyGradeDto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 
 * @author 편용림
 *
 */

public interface GradeRespository extends JpaRepository<GradeForScholarshipDto,Long> {
	
	// 학생이 수강한 연도 조회
	public List<GradeDto> selectSubYearByStudentId(Long studentId);
	
	// 학생이 수강한 학기 조회
	public List<GradeDto> selectSemesterByStudentId(Long studentId);
	
	// 금학기 성적 조회
	public List<GradeDto> selectGradeDtoBySemester(@Param("studentId") Long studentId, @Param("semester") Long semester, @Param("subYear") Long subYear);
	
	// 학기별 성적조회 (전체 조회)
	public List<GradeDto> selectGradeDtoByStudentId(Long studentId);
	
	// 학기별 성적조회 (선택 조회)
	public List<GradeDto> selectGradeDtoBytype(@Param("studentId") Long studentId,@Param("subYear") Long subYear,@Param("semester") Long semester,@Param("type") String type);

	// 전체 찾는거
	public List<GradeDto> selectGradeDtoByStudentIdAndSubYear(@Param("studentId") Long studentId, @Param("subYear") Long subYear, @Param("semester") Long semester);

	// 누계성적 조회
	public MyGradeDto  selectMyGradeDtoBySemester(@Param("studentId") Long studentId, @Param("subYear") Long subYear, @Param("semester") Long semester);
		
	/**
	 * @author 서영
	 * 장학금 유형 결정을 위한 성적 평균을 가져옴
	 */
	public GradeForScholarshipDto findAvgGradeByStudentIdAndSemester(@Param("studentId") Long studentId, @Param("subYear") Long subYear, @Param("semester") Long semester);

	
	
	// 전체 누계성적 조회
	public List<MyGradeDto> selectMyGradeDtoByStudentId(Long studentId);
	
	// 연도 누계성적 조회
	public List<MyGradeDto> gradeinquiryBysubYear(@Param("studentId") Long studentId, @Param("subYear") Long subYear);
	
	
}
