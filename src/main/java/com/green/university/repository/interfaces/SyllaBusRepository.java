package com.green.university.repository.interfaces;

import com.green.university.dto.SyllaBusFormDto;
import org.springframework.data.repository.CrudRepository;


/**
 * 
 * 
 * @author 박성희
 */

public interface SyllaBusRepository extends CrudRepository<SyllaBusFormDto,Long> {

	// 강의 등록 시, 강의 ID만 미리 저장
	public Long insertOnlySubId(Long subjectId);
	
	// 강의 삭제 시, 해당 강의 ID의 계획서 삭제
	public Long delete(Long subjectId);
	/**
	 * 강의계획서 업데이트
	 * @author 김지현
	 * @param syllaBusFormDto
	 * @return 실행 row count
	 */
	public Long updateSyllabus(SyllaBusFormDto syllaBusFormDto);

}
