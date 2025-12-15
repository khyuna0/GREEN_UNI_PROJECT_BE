package com.green.university.domain.admin.repository;

import com.green.university.domain.admin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
