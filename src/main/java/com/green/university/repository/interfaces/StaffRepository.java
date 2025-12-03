package com.green.university.repository.interfaces;

import com.green.university.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff,Long> {

    // id 찾기, 한 컬럼 찾는 용도라 id만 찾기로 바꾸지 않음
    Staff findByNameAndEmail(String name, String email);
    
	// password 발급용 엔티티 확인
    Staff findByIdAndNameAndEmail(Long id,String name, String email);

}
