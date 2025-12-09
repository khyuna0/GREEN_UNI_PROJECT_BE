package com.green.university.repository;

import com.green.university.entity.NoticeFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeFileRepository extends JpaRepository<NoticeFile, Long> {
    // 한가지 Repository에서 두개의 entity 관리 x (Notice , NoticeFile)
    // 공지 첨부파일 관리용 리파지토리

    // Optional : 하나만 존재 할수 있는 구조에서 없을 가능성까지 안전하게 표현
    Optional<NoticeFile> findByNotice_Id(Long noticeId);

    void deleteByNotice_Id(Long noticeId);
}
