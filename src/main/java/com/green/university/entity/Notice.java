package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.w3c.dom.Text;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;
    private Long views;

    // 타임스탬프 ..?
    private Timestamp createdTime; // 공지 생성시간

    // 파일목록
    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeFile> files = new ArrayList<>();

}
