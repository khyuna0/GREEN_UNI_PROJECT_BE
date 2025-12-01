package com.green.university.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;


@Entity
@Data
public class NoticeFile {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice; // 애노테이션 확인하기

    private String originFilename;
    private String uuidFilename;

}
