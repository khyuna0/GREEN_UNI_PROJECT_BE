package com.green.university.domain.tuition.repository;

import com.green.university.domain.tuition.entity.CollTuit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


// 단과대별 등록금 repository
public interface CollTuitRepository extends JpaRepository<CollTuit, Long> {

    // 해당 단과대(College)가 이미 등록금이 있는지 여부 (중복 체크)
    boolean existsByCollege_Id(Long collegeId);

    // 단과대 기준으로 등록금 하나 찾기
    Optional<CollTuit> findByCollege_Id(Long collegeId);

    // 단과대 기준으로 등록금 삭제
    void deleteByCollege_Id(Long collegeId);
}
