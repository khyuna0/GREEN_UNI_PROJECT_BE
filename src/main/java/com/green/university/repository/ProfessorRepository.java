package com.green.university.repository;

import com.green.university.entity.Professor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface ProfessorRepository extends JpaRepository<Professor, Long>, JpaSpecificationExecutor<Professor> {

    // id 찾기
    Long findByNameAndEmail(String name, String email);

    // password 찾기
    Long findByIdAndNameAndEmail(Long id, String name, String email);

    // 페이지, 과별 교수 조회 (검색 용)
    Page<Professor> findByDepartmentName(String departmentName, Pageable pageable);

}
