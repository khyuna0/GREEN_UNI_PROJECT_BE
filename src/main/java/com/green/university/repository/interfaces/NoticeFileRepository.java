package com.green.university.repository.interfaces;

import com.green.university.entity.NoticeFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeFileRepository extends JpaRepository<NoticeFile, Long> {
    // 한가지 Repository에서 두개의 entity 관리 x (Notice , NoticeFile)
    // 공지 첨부파일 관리용 리파지토리

    List<NoticeFile> findByNotice_Id(Long noticeId);

    void deleteByNotice_Id(Long noticeId);
}
