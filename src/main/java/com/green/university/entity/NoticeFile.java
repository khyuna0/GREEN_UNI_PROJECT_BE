package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class NoticeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;  // 어느 공지에 속한 파일인지

    private String originFilename;
    private String uuidFilename;

}
