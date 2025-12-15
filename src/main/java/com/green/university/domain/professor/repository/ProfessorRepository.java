package com.green.university.domain.professor.repository;

import com.green.university.domain.professor.entity.Professor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface ProfessorRepository extends JpaRepository<Professor, Long>, JpaSpecificationExecutor<Professor> {

    // id 찾기
    @Query("select p.id from Professor p where p.name = :name and p.email = :email")
    Long findIdByNameAndEmail(String name, String email);

    // password 찾기
    @Query("select p.id from Professor p where p.id = :id and p.name = :name and p.email = :email")
    Long findByIdAndNameAndEmail(Long id, String name, String email);

    // 페이지, 과별 교수 조회 (검색 용)
    Page<Professor> findByDepartmentName(String departmentName, Pageable pageable);

    // 이름으로 교수 찾기
    Optional<Professor> findByName(String name);

}
