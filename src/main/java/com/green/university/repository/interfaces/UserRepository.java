package com.green.university.repository.interfaces;

import com.green.university.dto.ChangePasswordDto;
import com.green.university.dto.response.PrincipalDto;
import com.green.university.entity.User;
import org.springframework.data.repository.CrudRepository;


public interface UserRepository extends CrudRepository<User,Long> {

	// id 이용해서 user_tb에 insert
	public Long insertToUser(User user);

}
