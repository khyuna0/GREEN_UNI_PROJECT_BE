package com.green.university.domain.notice.dto;

import com.green.university.domain.notice.entity.Notice;
import com.green.university.global.utils.LocalDateTimeUtil;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeDto {  // 응답용
    private Long id;
    private String category;
    private String title;
    private String content;
    private Long views;
    private LocalDateTime createdTime;
    private String createdTimeFormatted; // 화면 출력용 시간

    private NoticeFileDto file;
    private boolean hasFile;


    public NoticeDto(Notice n) {
        this.id = n.getId();
        this.category = n.getCategory();
        this.title = n.getTitle();
        this.content = n.getContent();
        this.views = n.getViews();
        this.createdTime = n.getCreatedTime();
        this.createdTimeFormatted = LocalDateTimeUtil.dateTimeToString(n.getCreatedTime());

        if (n.getFile() != null) {
            this.file = new NoticeFileDto(n.getFile());
            this.hasFile = true;
        } else {
            this.hasFile = false;
        }
    }
}
