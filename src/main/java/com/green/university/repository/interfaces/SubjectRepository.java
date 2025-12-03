package com.green.university.repository.interfaces;

import com.green.university.dto.AllSubjectSearchFormDto;
import com.green.university.dto.CurrentSemesterSubjectSearchFormDto;
import com.green.university.dto.SubjectFormDto;
import com.green.university.dto.response.ReadSyllabusDto;
import com.green.university.dto.response.SubjectDto;
import com.green.university.dto.response.SubjectForProfessorDto;
import com.green.university.dto.response.SubjectPeriodForProfessorDto;
import com.green.university.entity.Subject;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/*
 *  박성희
 *  강의 repository
 */


public interface SubjectRepository extends JpaRepository<Subject,Long> {
	/**
	 * 성희 
	 * 강의 입력 시 같은 강의실, 요일, 연도, 학기 정보 조회
	 */
	List<Subject> findByRoom_IdAndSubDayAndSubYearAndSemester(String room_id, String subDay, Long subYear, Long semester);

	/**
	 * 성희 
	 * 제일 최근 강의 ID 조회 - 이거 메서드 쓰는 거 맞나?
	 */
	Long findIdOrderById(Long id);

	/**
	 * @author 서영
	 * @return 수강 신청에 사용할 강의 정보
	 */
	List<Subject> findBySubYearAndSemester(Long subYear, Long semester);
	// 추후에 아래 페이지용으로 변경해야함
	Page<Subject> findBySubYearAndSemester(Long subYear, Long semester, Pageable pageable);

	/**
	 * @author 서영
	 * @return 전체 강의 정보
	 */
	Page<Subject> findAll(Pageable pageable);
	
	/**
	 * @author 김지현
	 * @return 교수 본인의 수업이 있는 년도-학기
	 */
	List<Subject> findByProfessor_Id(Long professorId);
	
	/**
	 * @author 김지현
	 * @return 그 학기의 본인 수업 정보들 (년도와 학기, 교수 id를 이용하여 해당 과목의 정보 불러오기) - SubjectPeriodForProfessorDto
	 */
	List<Subject> findByProfessor_IdAndSubYearAndSemester(Long professorId, Long subYear, Long semester);

	/**
	 * @author 서영
	 * @return 연도-학기-개설학과-강의명 검색을 조건으로 한 강의 정보
	 */
	List<Subject> findBySubYearAndSemesterAndDepartment_IdAndNameContaining(
			Long subYear, Long semester, Long deptId, String name);

	/**
	 * (currentSemesterSubjectSearchFormDto)
	 * @return 연도-학기-강의구분-개설학과-강의명 검색을 조건으로 한 강의 정보
	 */
	List<Subject> findBySubYearAndSemesterAndTypeAndDepartment_IdAndNameContaining(
			Long subYear, Long semester, String type, Long deptId, String name);

	/**
	 * @author 서영
	 * 현재 인원 수정 (1명 추가 or 삭제 or 0으로 초기화)
	 */
	// ============================================== 수정 해야함
	Long updateNumOfStudent(@Param("id") Long id, @Param("type") String type);

	/**
	 * @author 서영
	 * 정원 >= 신청인원인 강의의 id 리스트
	 */
	@Query("select s.id from Subject s where s.capacity >= s.numOfStudent")
	List<Long> findIdByCapacityGreaterThanOrEqualNumOfStudent();


	/**
	 * @author 서영
	 * 정원 < 신청인원인 강의의 id 리스트
	 */
	@Query("select s.id from Subject s where s.capacity < s.numOfStudent")
	List<Long> findIdByCapacityLessThanNumOfStudent();


}
