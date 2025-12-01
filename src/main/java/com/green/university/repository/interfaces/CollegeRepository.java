package com.green.university.repository.interfaces;

import com.green.university.dto.CollegeFormDto;
import com.green.university.repository.model.College;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

/*
 *  박성희
 *  단과대 repository
 */


public interface CollegeRepository extends JpaRepository<College,Long> {
	public Long insert(CollegeFormDto CollegeFormDto);

	public List<College> selectCollegeDto();

	public Long selectCollegeDtoByName(String name);
	public College selectCollegeDtoById(Long id);
}
