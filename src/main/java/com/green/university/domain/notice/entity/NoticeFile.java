package com.green.university.domain.notice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class NoticeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 공지 1개당 파일 1개 구조에 맞게 OneToOne으로 정합성 맞춤
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notice_id", nullable = false, unique = true)
    @JsonIgnore // JSON 무한순환 방지 핵심
    private Notice notice;  // 어느 공지에 속한 파일인지

    private String originFilename;
    private String uuidFilename;

}
