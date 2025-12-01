package com.green.university.repository.interfaces;

import com.green.university.dto.CreateStaffDto;
import com.green.university.dto.FindIdFormDto;
import com.green.university.dto.FindPasswordFormDto;
import com.green.university.dto.UserUpdateDto;
import com.green.university.dto.response.UserInfoForUpdateDto;
import com.green.university.repository.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Staff DAO
 * 
 * @author 김지현
 */

public interface StaffRepository extends JpaRepository<Staff,Long> {

	// staff_tb에 insert
	public Long insertToStaff(CreateStaffDto createStaffDto);

	// staff_tb에서 자동 생성된 id 받아오기
	public Long selectIdByCreateStaffDto(CreateStaffDto createStaffDto);

	// 업데이트용 정보 읽기
	public UserInfoForUpdateDto selectByUserId(Long userId);

	// 유저정보 업데이트
	public Long updateStaff(UserUpdateDto userUpdateDto);
	
	// id로 staff 모델 불러오기
	public Staff selectStaffById(Long Id);
	
	// id 찾기
	public Long selectIdByNameAndEmail(FindIdFormDto findIdFormDto);
	
	// password 발급용 model 확인
	public Long selectStaffByIdAndNameAndEmail(FindPasswordFormDto findPasswordFormDto);

}
