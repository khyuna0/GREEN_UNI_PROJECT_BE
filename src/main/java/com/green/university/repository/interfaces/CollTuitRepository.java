package com.green.university.repository.interfaces;

import com.green.university.dto.CollTuitFormDto;
import com.green.university.repository.model.College;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

/*
 *  박성희
 *  단과대별 등록금 repository
 */


public interface CollTuitRepository extends JpaRepository<College,Long> {
	public Long insert(CollTuitFormDto collTuitFormDto);
	public List<CollTuitFormDto> selectByCollTuitDto();
	public Long updateByCollTuitDto(CollTuitFormDto collTuitFormDto);
}
