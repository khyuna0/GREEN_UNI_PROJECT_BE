package com.green.university.domain.notice.service;

import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploaderService {

    private final S3Template s3Template;

    @Value("${custom.s3.bucket}") // yml에서 설정한 버킷 이름 가져오기
    private String bucket;

    // S3 업로드 후 전체 URL 반환
    public String upload(MultipartFile file, String dirName) throws IOException {
        String originalFileName = file.getOriginalFilename();
        // 파일명 중복 방지를 위한 UUID 생성
        String uuidFileName = dirName + "/" + UUID.randomUUID() + "_" + originalFileName;

        // S3에 업로드 (InputStream 방식)
        s3Template.upload(bucket, uuidFileName, file.getInputStream(), null);

        // 전체 URL 조합해서 반환 (CloudFront 쓴다면 도메인을 CloudFront로 바꾸면 더 좋음)
        return "https://" + bucket + ".ddskov9p76ko1.cloudfront.net/" + uuidFileName;
    }

    // 파일 삭제 (경로 포함된 파일명 필요, 예: notice/uuid_파일.jpg)
    public void delete(String fileName) {
        // null 체크
        if (fileName != null && !fileName.isBlank()) {
            s3Template.deleteObject(bucket, fileName);
        }
    }
}
