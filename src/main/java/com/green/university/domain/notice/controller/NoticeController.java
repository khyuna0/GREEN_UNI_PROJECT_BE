package com.green.university.domain.notice.controller;

import com.green.university.domain.notice.dto.NoticeDto;
import com.green.university.domain.notice.dto.NoticeFormDto;
import com.green.university.domain.notice.dto.NoticePageFormDto;
import com.green.university.domain.notice.entity.Notice;
import com.green.university.domain.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 공지사항 리스트 + 페이지
    @PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR', 'STAFF')")
    @GetMapping("/search/{page}")
    public ResponseEntity<?> showNoticeByKeywordAndPage(
            @RequestParam(defaultValue = "select") String crud,
            @PathVariable int page,
            NoticePageFormDto dto) {

        Page<Notice> noticePage = noticeService.readNoticePage(dto, page);

        return ResponseEntity.ok(Map.of(
                "crud", crud,
                "keyword", dto.getKeyword(),
                "type", dto.getType(),
                "noticeList", noticePage.getContent(),
                "listCount", noticePage.getTotalPages(),
                "currentPage", page
        ));
    }

    // 공지사항 페이지 이동
    @GetMapping("/list/{page}")
    public ResponseEntity<?> showNoticeListByPage(
            @RequestParam(defaultValue = "select") String crud,
            @PathVariable int page) {

        NoticePageFormDto dto = new NoticePageFormDto();
        dto.setKeyword(null);
        dto.setType(null);

        Page<Notice> noticePage = noticeService.readNoticePage(dto, page);

        return ResponseEntity.ok(Map.of(
                "crud", crud,
                "noticeList", noticePage.getContent(),
                "listCount", noticePage.getTotalPages(),
                "currentPage", page
        ));
    }

    // 공지사항 등록 (multipart는 @ModelAttribute로 받는 게 안전)
    // 파일관련은 service에서 -> 컨트롤러에 파일 관련 코드많아지면
    // 테스트 어려움 , 수정시 버그위험 증가, 다른api에도 중복 코드 많이 생김
    @PostMapping("/write")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> insertNotice(@ModelAttribute @Validated NoticeFormDto noticeFormDto) {

        noticeService.createNotice(noticeFormDto);
        return ResponseEntity.ok().body("공지사항 입력이 완료되었습니다");
    }

    /**
     *
     * @return 공지사항 상세 조회 기능
     */
    @GetMapping("/read/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR', 'STAFF')")
    public ResponseEntity<?> selectByIdNotice( @PathVariable("id") Long id) {
        NoticeDto notice = noticeService.readByIdNotice(id);
        notice.setContent(notice.getContent().replace("\r\n", "<br>"));

        return ResponseEntity.ok(Map.of(
                "crud", "read",
                "notice", notice
        ));
    }

    //  공지사항 수정 페이지
    @GetMapping("/update/{NoticeId}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> update(@PathVariable("NoticeId") Long noticeId) {

        NoticeDto notice = noticeService.readByIdNotice(noticeId);
        return ResponseEntity.ok(Map.of(
                "crud","update",
                "notice", notice
        ));
    }

    // 공지사항 수정
    @PatchMapping("/update/{NoticeId}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> update(@PathVariable("NoticeId") Long NoticeId,
                                    @ModelAttribute @Validated NoticeFormDto noticeFormDto) {
        noticeService.updateNotice(NoticeId, noticeFormDto);
        return ResponseEntity.ok().body("공지사항 수정이 완료되었습니다.");
    }

    // 공지사항 삭제
    @DeleteMapping("/delete/{NoticeId}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> delete(@PathVariable("NoticeId") Long NoticeId) {
        noticeService.deleteNotice(NoticeId);
        return ResponseEntity.ok().body("공지사항 삭제가 완료되었습니다.");
    }

    // 파일 다운로드(공지 ID 기준)
    @GetMapping("/file/download/{noticeId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR', 'STAFF')")
    public ResponseEntity<?> downloadNoticeFile(@PathVariable Long noticeId) {
        return noticeService.downloadFileByNoticeId(noticeId);
    }

}
