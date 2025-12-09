package com.green.university.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.w3c.dom.Text;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
    private LocalDateTime createdTime; // 공지 생성시간

    @OneToOne(mappedBy = "notice",
            cascade = CascadeType.ALL,  // 부모 저장/삭제할 때 자식도 자동 처리
            orphanRemoval = true,       // 부모에서 연결 끊긴 자식 DB에서 자동삭제
            fetch = FetchType.LAZY)
    private NoticeFile file;


    // 양방향 편의 메서드
    // 한쪽만 설정해도 반대쪽도 자동으로 맞춰줌
    public void setFile(NoticeFile file) {
        this.file = file;
        if (file != null) {
            file.setNotice(this);
        }
    }

}
