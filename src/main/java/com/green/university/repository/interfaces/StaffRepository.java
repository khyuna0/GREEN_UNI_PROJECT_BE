package com.green.university.repository.interfaces;

import com.green.university.dto.CreateStaffDto;
import com.green.university.dto.FindIdFormDto;
import com.green.university.dto.FindPasswordFormDto;
import com.green.university.dto.UserUpdateDto;
import com.green.university.dto.response.UserInfoForUpdateDto;
import com.green.university.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Staff DAO
 * 
 * @author 김지현
 */

public interface StaffRepository extends JpaRepository<Staff,Long> {

    
	// 업데이트용 정보 읽기
	public UserInfoForUpdateDto selectByUserId(Long userId);

	// 유저정보 업데이트
	public Long updateStaff(UserUpdateDto userUpdateDto);
	
	// id로 staff 모델 불러오기
	public Staff selectStaffById(Long Id);
	
	// id 찾기
	public Long selectIdByNameAndEmail(FindIdFormDto findIdFormDto);

    // id 찾기, 한 컬럼 찾는 용도라 id만 찾기로 바꾸지 않음
    public Staff findByNameAndEmail(String name, String email);
    
	// password 발급용 model 확인
	public Staff findByIdAndNameAndEmail(Long id,String name, String email);

}
