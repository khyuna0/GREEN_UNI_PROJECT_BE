package com.green.university.repository.interfaces;

import com.green.university.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 제목 검색
    Page<Notice> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    // 제목 + 내용 검색 (keyword)
    Page<Notice> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String titleKeyword,
            String contentKeyword,
            Pageable pageable
    );

    // 최신 공지 5개
    List<Notice> findTop5ByOrderByCreatedTimeDesc();
}
