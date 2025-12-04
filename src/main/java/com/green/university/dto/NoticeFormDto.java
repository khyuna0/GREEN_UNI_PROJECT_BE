package com.green.university.dto;

import com.green.university.utils.TimestampUtil;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.sql.Timestamp;


@Data
public class NoticeFormDto { // 공지사항 수정, 입력, 출력 시 사용 

    private Long id; // 프론트 호출용으로 사용하는게 있어서 남겨놓음
    private Long noticeId;
    private String category;
    @NotEmpty
    @Size(max = 50)
    private String title;
    @NotEmpty
    private String content;
    private Long views;
    private Timestamp createdTime;
    private MultipartFile file;
    private String originFilename;
    private String uuidFilename;

    // 공지 시간 처리 (날짜 시간)
    public String timeFormat() {
        TimestampUtil timestampUtil = new TimestampUtil();
        return timestampUtil.dateTimeToString(createdTime);
    }

    // 공지 시간 처리 (날짜)
    public String dateFormat() {
        TimestampUtil timestampUtil = new TimestampUtil();
        return timestampUtil.dateToString(createdTime);
    }

}
