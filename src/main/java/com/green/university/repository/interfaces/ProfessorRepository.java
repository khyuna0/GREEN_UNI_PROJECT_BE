package com.green.university.repository.interfaces;

import com.green.university.dto.*;
import com.green.university.dto.response.ProfessorInfoDto;
import com.green.university.dto.response.UserInfoForUpdateDto;
import com.green.university.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

/**
 * Professor DAO
 * 
 * @author 김지현
 */

public interface ProfessorRepository extends JpaRepository<Professor,Long> {

	// staff_tb에 insert
	public Long insertToProfessor(CreateProfessorDto createProfessorDto);

	// staff_tb에서 자동 생성된 id 받아오기
	public Long selectIdByCreateProfessorDto(CreateProfessorDto createProfessorDto);

	// 업데이트용 정보 읽기
	public UserInfoForUpdateDto selectByUserId(Long userId);

	// 유저정보 업데이트
	public Long updateProfessor(UserUpdateDto userUpdateDto);

	// Professor model 정보 id로 조회
	public Professor selectProfessorById(Long id);

	// ProfessorInfoDto id로 조회
	public ProfessorInfoDto selectProfessorInfoById(Long id);

	// id 찾기
	public Long selectIdByNameAndEmail(FindIdFormDto findIdFormDto);

	// password 발급용 model 확인
	public Long selectProfessorByIdAndNameAndEmail(FindPasswordFormDto findPasswordFormDto);

	// 페이지별 교수 조회
	public List<Professor> selectProfessorList(ProfessorListForm professorListForm);

	// 페이지, 과별 교수조회
	public List<Professor> selectByDepartmentId(ProfessorListForm professorListForm);

	// id로 교수 조회
	public List<Professor> selectByProfessorId(ProfessorListForm professorListForm);

	// 페이징 처리 위한 전체 교수 수 조회
	public Long selectProfessorAmount();

	// 페이징 처리 위한 과 교수 수 조회
	public Long selectProfessorAmountByDeptId(Long deptId);

}
