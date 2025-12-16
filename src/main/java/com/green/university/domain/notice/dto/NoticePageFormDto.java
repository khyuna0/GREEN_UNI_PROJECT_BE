package com.green.university.domain.notice.dto;

import lombok.Data;

@Data
public class NoticePageFormDto {

    private String keyword;
    private String type; // title / content / all

}
