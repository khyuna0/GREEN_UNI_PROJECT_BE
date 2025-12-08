package com.green.university.service;

import com.green.university.exception.CustomRestfullException;
import com.green.university.repository.interfaces.CollegeRepository;
import com.green.university.repository.interfaces.DepartmentRepository;
import com.green.university.entity.College;
import com.green.university.entity.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 
 * @author 서영
 *
 */

@Service
public class CollegeService {

	@Autowired
	private CollegeRepository collegeRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

    
    //id로 해당 단과대 정보 가져옴
	public College readCollById(Long id) {
        return collegeRepository.findById(id)
                .orElseThrow(()-> new CustomRestfullException("해당 단과대를 찾을 수 없습니다.", HttpStatus.NOT_FOUND, "/break/appList"));
	}


    //id로 해당 학과 정보 가져옴 , deptId
	public Department readDeptById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new CustomRestfullException("해당 학과를 찾을 수 없습니다.",HttpStatus.NOT_FOUND, "/break/appList"));
	}


    //전체 학과 정보 조회
	public List<Department> readDeptAll() {
		List<Department> deptEntityList = departmentRepository.findAll();
		return deptEntityList;
	}

}
