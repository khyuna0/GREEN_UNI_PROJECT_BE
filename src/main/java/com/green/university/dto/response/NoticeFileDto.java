package com.green.university.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NoticeFileDto {
    // 프론트에서 보여줄내용 -> 파일명 + 다운로드 식별자
    private Long id;
    private String originFilename;
}

