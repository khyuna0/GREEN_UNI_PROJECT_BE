package com.green.university.repository.interfaces;

import com.green.university.dto.DepartmentFormDto;
import com.green.university.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

/*
 *  박성희
 *  학과 repository
 */


public interface DepartmentRepository extends JpaRepository<Department,Long> {
	public Long insert(DepartmentFormDto departmentFormDto);
	
	public Department selectById(Long id);
	public List<Department> selectByDepartmentDto();
	public Long updateByDepartmentDto(DepartmentFormDto departmentFormDto);
	
}
