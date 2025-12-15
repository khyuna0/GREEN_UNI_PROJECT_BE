package com.green.university.domain.university.repository;

import com.green.university.domain.university.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // 학과 이름 중복 체크
    boolean existsByName(String name);

    // 이름으로 학과 찾기
    Optional<Department> findByName(String name);

    // 단과대 ID 오름차순 정렬
    List<Department> findAllByOrderByCollege_IdAsc();

}
