package com.green.university.dto.response;

import com.green.university.entity.Notice;
import com.green.university.entity.NoticeFile;
import com.green.university.utils.LocalDateTimeUtil;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class NoticeDto {

    private Long id;
    private String category;
    private String title;
    private String content;
    private Long views;
    private LocalDateTime createdTime;

    private String createdTimeFormatted; // 화면 출력용

    private List<NoticeFile> files = new ArrayList<>();

    public NoticeDto(Notice n) {
        this.id = n.getId();
        this.category = n.getCategory();
        this.title = n.getTitle();
        this.content = n.getContent();
        this.views = n.getViews();
        this.createdTime = n.getCreatedTime();
        this.createdTimeFormatted = LocalDateTimeUtil.dateTimeToString(n.getCreatedTime());

        if (n.getFiles() != null) {
            this.files = new ArrayList<>(n.getFiles());
        } else {
            this.files = null;
        }
    }
}
