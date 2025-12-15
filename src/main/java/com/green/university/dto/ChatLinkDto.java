package com.green.university.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatLinkDto {

    private String label; // 버튼 텍스트
    private String path;  // 라우터 이동경로

}
