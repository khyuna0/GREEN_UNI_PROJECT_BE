package com.green.university.domain.admin.repository;

import com.green.university.domain.admin.entity.SugangPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SugangPeriodRepository extends JpaRepository<SugangPeriod,Long> {
    Optional<SugangPeriod> findFirstByOrderByIdDesc(); // 최신 상태 조회
}
