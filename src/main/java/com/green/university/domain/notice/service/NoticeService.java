package com.green.university.domain.notice.service;

import com.green.university.domain.notice.dto.NoticeDto;
import com.green.university.domain.notice.dto.NoticeFormDto;
import com.green.university.domain.notice.dto.NoticePageFormDto;
import com.green.university.domain.notice.entity.Notice;
import com.green.university.domain.notice.entity.NoticeFile;
import com.green.university.domain.notice.repository.NoticeFileRepository;
import com.green.university.domain.notice.repository.NoticeRepository;
import com.green.university.domain.notice.specification.NoticeSpecification;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.utils.Define;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeFileRepository noticeFileRepository;

    private static final int PAGE_SIZE = 10;

    // 검색 + 페이징 처리
    @Transactional(readOnly = true)
    public Page<Notice> readNoticePage(NoticePageFormDto dto, int page) {

        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        String keyword = dto.getKeyword();
        String type = dto.getType();

        Specification<Notice> spec = (root, query, cb) -> null;

        // keyword 있을 때만 type에 따라 spec 사용
        if (keyword != null && !keyword.isBlank()) {

            if ("title".equalsIgnoreCase(type)) {
                spec = spec.and(NoticeSpecification.titleContains(keyword));

            } else if ("content".equalsIgnoreCase(type)) {
                spec = spec.and(NoticeSpecification.contentContains(keyword));

            } else {
                // 기본: 제목 + 내용
                spec = spec.and(NoticeSpecification.titleOrContentContains(keyword));
            }
        }

        return noticeRepository.findAll(spec, pageable);
    }

    // 공지 등록
    @Transactional
    public void createNotice(@Validated NoticeFormDto noticeFormDto) {

        Notice notice = new Notice();
        notice.setCategory(noticeFormDto.getCategory());
        notice.setTitle(noticeFormDto.getTitle());
        notice.setContent(noticeFormDto.getContent());

        Long views =  noticeFormDto.getViews();
        notice.setViews(views != null ? views : 0L);

        // createTime 비었으면 지금 시간으로
        notice.setCreatedTime(noticeFormDto.getCreatedTime() != null
                ? noticeFormDto.getCreatedTime(): LocalDateTime.now());

        // 먼저 notice 저장
        Notice saved = noticeRepository.save(notice);

        // 파일 있으면 저장 + notice 연결
        MultipartFile file = noticeFormDto.getFile();
        if( file != null && !file.isEmpty()) {
            validateFileSize(file);

            StoredFileInfo info = storeFileToDisk(file);

            NoticeFile noticeFile = new NoticeFile();
            noticeFile.setOriginFilename(info.originName);
            noticeFile.setUuidFilename(info.uuidName);

            //notice.setFile이 양방향 연결까지 처리
            saved.setFile(noticeFile);

            //cascade로 NoticeFile 저장
            noticeRepository.save(saved);

            //DTO에도 필요하면 채워줌
            noticeFormDto.setOriginFilename(info.originName);
            noticeFormDto.setUuidFilename(info.uuidName);
        }

        noticeFormDto.setNoticeId(saved.getId());
        noticeFormDto.setId(saved.getId());
    }

    // 공지 상세 조회 + 조회수 증가
    @Transactional
    public NoticeDto readByIdNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomRestfullException("공지 없음", HttpStatus.NOT_FOUND));

        long currentViews = notice.getViews() == null ? 0: notice.getViews();
        notice.setViews(currentViews + 1);

        return new NoticeDto(notice);
    }

    // 공지 수정
    @Transactional
    public void updateNotice(Long id, NoticeFormDto noticeFormDto) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomRestfullException("공지 없음", HttpStatus.NOT_FOUND));

        notice.setCategory(noticeFormDto.getCategory());
        notice.setTitle(noticeFormDto.getTitle());
        notice.setContent(noticeFormDto.getContent());

        MultipartFile newFile = noticeFormDto.getFile();
        boolean removeFile = Boolean.TRUE.equals(noticeFormDto.getRemoveFile());

        // 파일 올렸던거 제거
        if (removeFile) {
            removeExistingFile(notice);
        }

        // 교체: 새 파일 올라오면 기존 제거 후 새로 저장
        if (newFile != null && !newFile.isEmpty()) {
            validateFileSize(newFile);

            // 기존 파일 있으면 제거(디스크+DB)
            removeExistingFile(notice);

            StoredFileInfo info = storeFileToDisk(newFile);

            NoticeFile noticeFile = new NoticeFile();
            noticeFile.setOriginFilename(info.originName);
            noticeFile.setUuidFilename(info.uuidName);

            notice.setFile(noticeFile); // 양방향 연결
        }
    }

    // 파일 처리 메서드들
    // 로직들이 공지에 종속되어있어서 일단 여기에 두는게 나음
    // 빼려고 하면 FileStorageService로 분리
    private void removeExistingFile(Notice notice) {
        if (notice.getFile() == null) return;

        NoticeFile old = notice.getFile();

        // 기존 파일 삭제(디스크)
        deleteFileFromDisk(old.getUuidFilename());

        // 기존 파일 엔티티 제거(orphanRemoval로 DB에서 삭제됨 기대)
        // mappedBy 구조라 orphanRemoval 타이밍/순서 이슈가 생길 수 있어서 delete+flush로 확실히 처리
        notice.setFile(null);

        // DB에서 기존 notice_file 행 먼저 삭제
        noticeFileRepository.delete(old);

        // delete가 먼저 반영되도록 flush (insert보다 앞서게)
        noticeFileRepository.flush();
    }


    // 공지 삭제  + 파일 삭제
    @Transactional
    public void deleteNotice(Long id) {

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomRestfullException("공지사항이 없습니다.",HttpStatus.NOT_FOUND));

        if(notice.getFile() != null) {
            deleteFileFromDisk(notice.getFile().getUuidFilename());
        }
        noticeRepository.delete(notice);
    }

    // 최근글 5개 조회
    public List<NoticeFormDto> readCurrentNotice() {
        List<Notice> noticeList = noticeRepository.findTop5ByOrderByCreatedTimeDesc();

        return noticeList.stream()
                .map(n -> {
                    NoticeFormDto dto = new NoticeFormDto();
                    dto.setId(n.getId());
                    dto.setNoticeId(n.getId());
                    dto.setCategory(n.getCategory());
                    dto.setTitle(n.getTitle());
                    dto.setContent(n.getContent());
                    dto.setViews(n.getViews());
                    dto.setCreatedTime(n.getCreatedTime());

                    return dto;
                })
                .collect(Collectors.toList());
    }


    // 파일 처리 메서드들
    // 로직들이 공지에 종속되어있어서 일단 여기에 두는게 나음
    // 빼려고 하면 FileStorageService로 분리

    // 파일 사이즈 제한
    private void validateFileSize(MultipartFile file){
        if(file.getSize() > Define.MAX_FILE_SIZE){
            throw new CustomRestfullException("파일 크기는 20MB 이상 클 수 없습니다.",HttpStatus.BAD_REQUEST);
        }
    }

    // 파일 저장 결과를 묶어두는 작은 내부 클래스
    private static class StoredFileInfo {
        String originName;
        String uuidName;
        StoredFileInfo(String originName, String uuidName){
            this.originName = originName;
            this.uuidName = uuidName;
        }
    }

    private StoredFileInfo storeFileToDisk(MultipartFile file){
        try {
            String saveDirectory = Define.UPLOAD_DIRECTORY;

            File dir = new File(saveDirectory);
            if(!dir.exists()){
                dir.mkdirs();
            }

            String origin = file.getOriginalFilename();
            String uuid = UUID.randomUUID() + "_" + origin;

            String uploadPath = saveDirectory + File.separator + uuid;
            File destination = new File(uploadPath);

            file.transferTo(destination);

            return new StoredFileInfo(origin,uuid);
        } catch (Exception e) {
            throw new CustomRestfullException("파일 저장 중 오류가 발생했습니다.",HttpStatus.BAD_REQUEST);
        }
    }

    //  첨부파일 다운로드 (noticeId 기준)
    public ResponseEntity<Resource> downloadFileByNoticeId(Long noticeId) {

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new CustomRestfullException("공지 사항이 존재하지 않습니다.", HttpStatus.NOT_FOUND));

        if (notice.getFile() == null) {
            throw new CustomRestfullException("첨부파일 없음", HttpStatus.NOT_FOUND);
        }

        String uuid = notice.getFile().getUuidFilename();
        String origin = notice.getFile().getOriginFilename();

        File file = new File(Define.UPLOAD_DIRECTORY + File.separator + uuid);
        if (!file.exists()) {
            throw new CustomRestfullException("파일이 존재하지 않습니다.", HttpStatus.NOT_FOUND);
        }

        Resource resource = new FileSystemResource(file);

        String encoded = URLEncoder.encode(origin, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    // 파일 삭제
    private void deleteFileFromDisk(String uuidFilename){
        if(uuidFilename == null) return;

        String path = Define.UPLOAD_DIRECTORY + File.separator + uuidFilename;
        File file = new File(path);

        if(file.exists()){
            file.delete();
        }
    }
}
