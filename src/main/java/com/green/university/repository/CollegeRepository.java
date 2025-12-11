package com.green.university.repository;

import com.green.university.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


// 단과대 repository
public interface CollegeRepository extends JpaRepository<College, Long> {

    // 단과대 이름 중복 체크용
    boolean existsByName(String name);

    // 이름으로 단과대 찾기
    Optional<College> findByName(String name);

}
