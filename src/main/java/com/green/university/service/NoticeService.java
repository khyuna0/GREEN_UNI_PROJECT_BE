package com.green.university.service;

import com.green.university.dto.NoticeFormDto;
import com.green.university.dto.NoticePageFormDto;
import com.green.university.dto.response.NoticeDto;
import com.green.university.exception.CustomRestfullException;
import com.green.university.repository.interfaces.NoticeRepository;
import com.green.university.entity.Notice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeService {

    @Autowired
    private NoticeRepository noticeRepository;

    private static final int PAGE_SIZE = 10;

    // 공지 입력
    public void readNotice(@Validated NoticeFormDto noticeFormDto) {

        Notice notice = new Notice();
        notice.setCategory(noticeFormDto.getCategory());
        notice.setTitle(noticeFormDto.getTitle());
        notice.setContent(noticeFormDto.getContent());

        Long views =  noticeFormDto.getViews();
        notice.setViews(views != null ? views : 0L);

        // createTime 비었으면 지금 시간으로
        notice.setCreatedTime(noticeFormDto.getCreatedTime() != null
                ? noticeFormDto.getCreatedTime() : Timestamp.from(Instant.now()));

        Notice saved = noticeRepository.save(notice);
        noticeFormDto.setNoticeId(saved.getId());
    }


    // 검색 + 페이징 처리
    public Page<Notice> readNoticePage(NoticePageFormDto noticePageFormDto){

        int pageParam = noticePageFormDto.getPage();
        int page = pageParam < 1 ? 1 : pageParam;

        String keyword = noticePageFormDto.getKeyword();
        String type = noticePageFormDto.getType();

        Pageable pageable = PageRequest.of(
                page - 1,          // 1페이지 -> 0, 2페이지 -> 1 ...
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC,"id") // 최신 글 순
        );

        // 검색어 없으면 전체 조회
        if (keyword == null || keyword.trim().isEmpty()) {
            return noticeRepository.findAll(pageable);
        }

        // 검색어 있으면 타입에 따라 나눔
        if ("title".equals(type)) {
            // 제목으로만 검색
            return noticeRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else {
            // 제목 + 내용으로 검색
            return noticeRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                    keyword, keyword, pageable
            );
        }
    }


    // 공지 갯수 확인
    public Long readNoticeAmount(NoticePageFormDto noticePageFormDto) {
        return readNoticePage(noticePageFormDto).getTotalElements();
    }


    // 공지 검색
    public List<Notice> readNoticeByKeyword(NoticePageFormDto noticePageFormDto) {
        return readNoticePage(noticePageFormDto).getContent();
    }


    // 공지 상세 조회 + 조회수 증가
    @Transactional
    public NoticeDto readByIdNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomRestfullException("공지 없음", HttpStatus.NOT_FOUND));

        long currentViews = notice.getViews() == null ? 0: notice.getViews();
        notice.setViews(currentViews + 1);

        NoticeDto noticeDto = new NoticeDto(notice);
        return noticeDto;
    }

    // 공지 수정
    @Transactional
    public void updateNotice(Long id, NoticeFormDto noticeFormDto) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CustomRestfullException("공지 없음", HttpStatus.NOT_FOUND));

        notice.setCategory(noticeFormDto.getCategory());
        notice.setTitle(noticeFormDto.getTitle());
        notice.setContent(noticeFormDto.getContent());
    }


    // 공지 삭제
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
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

}
