package com.green.university.domain.breakapp.repository;

import com.green.university.domain.breakapp.entity.BreakApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface BreakAppRepository extends JpaRepository<BreakApp, Long> {

    // 학생의 휴학 신청 목록조회
    List<BreakApp> findByStudent_IdOrderByIdDesc(Long studentId);

    // 처리되지 않은 휴학 신청 조회하기 (교직원용)
    List<BreakApp> findByStatus(String status);


}
