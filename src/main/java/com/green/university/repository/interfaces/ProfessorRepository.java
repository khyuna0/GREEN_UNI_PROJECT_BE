package com.green.university.repository.interfaces;

import com.green.university.dto.*;
import com.green.university.dto.response.ProfessorInfoDto;
import com.green.university.dto.response.UserInfoForUpdateDto;
import com.green.university.entity.Professor;
import com.green.university.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

/**
 * Professor DAO
 * 
 * @author 김지현
 */

public interface ProfessorRepository extends JpaRepository<Professor,Long> {

	// id 찾기
	public Professor findByNameAndEmail(String name, String email);

    // password 발급용 엔티티? 확인
    public Staff findByIdAndNameAndEmail(Long id, String name, String email);

	// 페이지, 과별 교수 조회 (검색 용)
	public List<Professor> findByDepartment_Id(Long departmentId);

	// 페이징 처리 위한 과 교수 수 조회
	public Long  countByDepartment_Id(Long departmentId);

}
