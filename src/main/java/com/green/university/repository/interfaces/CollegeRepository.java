package com.green.university.repository.interfaces;

import com.green.university.dto.CollegeFormDto;
import com.green.university.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

/*
 *  박성희
 *  단과대 repository
 */


public interface CollegeRepository extends JpaRepository<College,Long> {

    // 단과대 이름 중복 체크용
    boolean existsByName(String name);

    //public List<College> selectCollegeDto(); 단과대조회, 중복검사에 씀
    //public int selectCollegeDtoByName(String name);
    //public College selectCollegeDtoById(Integer id);


}
