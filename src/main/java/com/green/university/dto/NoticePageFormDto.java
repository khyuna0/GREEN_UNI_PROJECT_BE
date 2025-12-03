package com.green.university.dto;

import lombok.Data;

@Data
public class NoticePageFormDto {

    // 페이징 처리
    private Long page;
    private String keyword;
    private String type;

}
