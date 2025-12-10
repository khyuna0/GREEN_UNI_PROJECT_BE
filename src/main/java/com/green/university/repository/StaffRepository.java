package com.green.university.repository;

import com.green.university.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StaffRepository extends JpaRepository<Staff,Long> {

    // id 찾기
    @Query("select st.id from Staff st where st.name = :name and st.email = :email")
    Long findIdByNameAndEmail(String name, String email);
    
	// password 찾기
    @Query("select st.id from Staff st where st.id = :id and st.name = :name and st.email = :email")
    Long findByIdAndNameAndEmail(Long id,String name, String email);

}
