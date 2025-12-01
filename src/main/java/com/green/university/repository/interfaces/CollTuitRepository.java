package com.green.university.repository.interfaces;

import com.green.university.dto.CollTuitFormDto;
import com.green.university.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

/*
 *  박성희
 *  단과대별 등록금 repository
 */
public interface CollTuitRepository extends JpaRepository<College,Long> {

    //등록금 중복?
	public List<CollTuitFormDto> selectByCollTuitDto();
}
