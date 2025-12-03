package com.green.university.repository.interfaces;

import com.green.university.entity.CollTuit;
import com.green.university.entity.Tuition;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author 서영
 *
 */


public interface TuitionRepository extends CrudRepository<Tuition,Long> {

	// 특정 학생의 등록금 내역 조회 (서비스에만 있고 컨트롤러에서는 사용안함... 나중에 필요없으면 지우기!)
	public List<Tuition> findByStudent_Id(Long studentId);

	// 특정 학생의 등록금 납부(완료) 내역 조회 (status가 true 인 것들만 조회한다)
    public List<Tuition> findByStudent_IdAndStatus(Long studentId, boolean status);

    // 특정 학생의 현재 년도? (tuiYear), 학기 별 등록금 조회 (등록금 고지서 용도)
    public Tuition findByStudent_IdAndTuiYearAndSemester(Long studentId, Long tuiYear, Long semester);

}