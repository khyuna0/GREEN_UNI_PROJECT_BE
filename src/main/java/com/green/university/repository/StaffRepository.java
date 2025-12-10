package com.green.university.repository;

import com.green.university.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff,Long> {

    // id 찾기
    Long findByNameAndEmail(String name, String email);
    
	// password 찾기
    Long findByIdAndNameAndEmail(Long id,String name, String email);

}
