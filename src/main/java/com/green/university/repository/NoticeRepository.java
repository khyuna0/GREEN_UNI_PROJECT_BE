package com.green.university.repository;

import com.green.university.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long>, JpaSpecificationExecutor<Notice> {

    // 최신 공지 5개
    List<Notice> findTop5ByOrderByCreatedTimeDesc();
}
