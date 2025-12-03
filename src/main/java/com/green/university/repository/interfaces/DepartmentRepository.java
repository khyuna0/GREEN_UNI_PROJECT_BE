package com.green.university.repository.interfaces;

import com.green.university.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DepartmentRepository extends JpaRepository<Department, Long> {
    // public Long insert(DepartmentFormDto departmentFormDto);
    // public Department selectById(Long id);
    // public List<Department> selectByDepartmentDto(); // 같은학과 이름 중복검사 , 학과조회
    // public Long updateByDepartmentDto(DepartmentFormDto departmentFormDto); //학과 수정

    // 학과 이름 중복 체크
    boolean existsByName(String name);


}
