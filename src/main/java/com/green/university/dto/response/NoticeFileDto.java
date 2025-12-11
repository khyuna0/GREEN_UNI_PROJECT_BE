package com.green.university.dto.response;

import com.green.university.entity.NoticeFile;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class NoticeFileDto {
    private Long id;
    private String originFilename; // 원본 파일명
    private String uuidFilename; // 서버 저장용 파일명(중복 충돌 금지)

    public NoticeFileDto(NoticeFile f) {
        this.id = f.getId();
        this.originFilename = f.getOriginFilename();
        this.uuidFilename = f.getUuidFilename();
    }
}


