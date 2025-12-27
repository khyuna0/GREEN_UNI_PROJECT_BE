package com.green.university.domain.notice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class NoticeFormDto { // 공지사항 수정, 입력, 출력 시 사용

    private Long id; // 프론트 호출용으로 사용하는게 있어서 남겨놓음
    private Long noticeId;
    private String category;

    @NotEmpty
    @Size(max = 50)
    private String title;

    @NotEmpty
    private String content;

    private Long views;
    private LocalDateTime createdTime;

    private MultipartFile file;
    private String originFilename;  // 원본 파일명
    private String uuidFilename;    // 서버 저장용 파일명(충돌방지)

    // 프론트에서 removeFile=true 보내면, 파일 삭제
    private Boolean removeFile;
}
