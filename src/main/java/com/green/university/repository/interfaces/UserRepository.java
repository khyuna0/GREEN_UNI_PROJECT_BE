package com.green.university.repository.interfaces;

import com.green.university.dto.ChangePasswordDto;
import com.green.university.dto.response.PrincipalDto;
import com.green.university.repository.model.User;
import org.springframework.data.repository.CrudRepository;


public interface UserRepository extends CrudRepository<User,Long> {
	
	// 로그인용
	public PrincipalDto selectById(Long userId);
	
	// 패스워드 변경
	public Long updatePassword(ChangePasswordDto changePasswordDto);
	
	// id 이용해서 user_tb에 insert
	public Long insertToUser(User user);

}
