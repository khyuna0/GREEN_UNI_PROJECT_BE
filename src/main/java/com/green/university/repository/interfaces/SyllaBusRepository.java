package com.green.university.repository.interfaces;

import com.green.university.dto.SyllaBusFormDto;
import com.green.university.entity.SyllaBus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 
 * 
 * @author 박성희
 */

public interface SyllaBusRepository extends JpaRepository<SyllaBus,Long> {

	/**
	 * 강의계획서 업데이트
	 * @author 김지현
	 * @param syllaBusFormDto
	 * @return 실행 row count
	 */

	/**
	 * 과목명으로 강의계획서 찾기
	 * */
	Optional<SyllaBus> findBySubject_Id(Long subjectId);
}
