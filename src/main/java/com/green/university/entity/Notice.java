package com.green.university.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;

    private String title;

    // 추후 TEXT로 써야함(내용이 긴가봄)
    private String content;

    private Long views;

    // 타임스탬프 ..
    private Timestamp createdTime;


    // 이미지 관련.. 필드를 써야할 것 같은데..
    // private NoticeFile noticeFile


}
