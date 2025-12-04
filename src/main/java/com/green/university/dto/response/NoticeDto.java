package com.green.university.dto.response;

import com.green.university.entity.Notice;
import com.green.university.entity.NoticeFile;
import com.green.university.entity.Professor;
import com.green.university.entity.Subject;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class NoticeDto {

    private Long id;
    private String category;
    private String title;
    private String content;
    private Long views;
    private Timestamp createdTime;
    private List<NoticeFile> files = new ArrayList<>();

    public NoticeDto(Notice n) {
        this.id = n.getId();
        this.category = n.getCategory();
        this.title = n.getTitle();
        this.content = n.getContent();
        this.views = n.getViews();
        this.createdTime = n.getCreatedTime();

//        // 파일 리스트가 null일 가능성 대비
//        if (n.getFiles() != null) {
//            this.files = n.getFiles().stream()
//                    .map(f -> new NoticeFile())
//                    .collect(Collectors.toList());
//        }

        // 엔티티 그대로 담는 경우
        if (n.getFiles() != null) {
            this.files = new ArrayList<>(n.getFiles());
        } else {
            this.files = null;
        }
    }


}

