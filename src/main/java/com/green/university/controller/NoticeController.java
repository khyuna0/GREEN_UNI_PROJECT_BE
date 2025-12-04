package com.green.university.controller;

import com.green.university.dto.NoticeFormDto;
import com.green.university.dto.NoticePageFormDto;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.entity.Notice;
import com.green.university.service.NoticeService;
import com.green.university.utils.Define;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notice")
public class NoticeController {
    @Autowired
    NoticeService noticeService;

    /**
     *
     * @return 공지사항 페이지
     */
    @GetMapping("")
    public ResponseEntity<?> notice(
                         @RequestParam(defaultValue = "select") String crud,
                         @RequestParam(defaultValue = "1") int page) {

        model.addAttribute("crud", crud);

        NoticePageFormDto dto = new NoticePageFormDto();
        dto.setPage(page);          // 1페이지부터 시작
        dto.setKeyword(null);       // 기본은 검색 없음
        dto.setType(null);

        Page<Notice> noticePage = noticeService.readNoticePage(dto);

        model.addAttribute("noticeList", noticePage.getContent());
        model.addAttribute("listCount", noticePage.getTotalPages());   // 총 페이지 수
        model.addAttribute("currentPage", page);

        return "/board/notice";
    }

    /**
     *
     * @return 공지사항 입력 기능
     */
    @PostMapping("/write")
    public ResponseEntity<?> insertNotice(@Validated NoticeFormDto noticeFormDto) {

        MultipartFile file = noticeFormDto.getFile();
        if (file.isEmpty() == false) {
            if (file.getSize() > Define.MAX_FILE_SIZE) {
                throw new CustomRestfullException("파일 크기는 20MB 이상 클 수 없습니다.", HttpStatus.BAD_REQUEST);
            }
            try {
                String saveDirectory = Define.UPLOAD_DIRECTORY;
                File dir = new File(saveDirectory);
                if (dir.exists() == false) {
                    dir.mkdirs();
                }
                UUID uuid = UUID.randomUUID();
                String fileName = uuid + "_" + file.getOriginalFilename();
                String uploadPath = Define.UPLOAD_DIRECTORY + File.separator + fileName;
                File destination = new File(uploadPath);
                file.transferTo(destination);
                noticeFormDto.setOriginFilename(file.getOriginalFilename());
                noticeFormDto.setUuidFilename(fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        noticeService.readNotice(noticeFormDto);
        return "redirect:/notice";
    }

    /**
     *
     * @return 공지사항 상세 조회 기능
     */
    @GetMapping("/read")
    public ResponseEntity<?> selectByIdNotice( @RequestParam Long id) {
        model.addAttribute("crud", "read");
        model.addAttribute("id", id);
        Notice notice = noticeService.readByIdNotice(id); // Todo 엔티티 말고 따로 dto 만들어서 처리
        if (notice == null) {
            model.addAttribute("notice", null);
        } else {
            model.addAttribute("notice", notice);
        }
        notice.setContent(notice.getContent().replace("\r\n", "<br>"));

        return "/board/notice";
    }

    // 공지사항 페이지 이동
    @GetMapping("/list/{page}")
    public ResponseEntity<?> showNoticeListByPage(
                                       @RequestParam(defaultValue = "select") String crud,
                                       @PathVariable int page) {

        model.addAttribute("crud", crud);

        NoticePageFormDto noticeFormDto = new NoticePageFormDto();
        noticeFormDto.setPage(page);
        noticeFormDto.setKeyword(null);
        noticeFormDto.setType(null);

        Page<Notice> noticePage = noticeService.readNoticePage(noticeFormDto);
        model.addAttribute("noticeList",noticePage.getContent());
        model.addAttribute("listCount",noticePage.getTotalPages());
        model.addAttribute("currentPage",page);

        return "/board/notice";
    }

    // 공지사항 검색 기능
    @GetMapping("/search")
    public ResponseEntity<?> showNoticeByKeyword( NoticePageFormDto noticePageFormDto) {

        noticePageFormDto.setPage(1); // 첫페이지는 1페이지

        Page<Notice> noticePage = noticeService.readNoticePage(noticePageFormDto);
        model.addAttribute("crud", "selectKeyword");
        model.addAttribute("keyword", noticePageFormDto.getKeyword());
        model.addAttribute("type",noticePageFormDto.getType());
        noticePageFormDto.setPage(0);

        model.addAttribute("noticeList",noticePage.getContent());
        model.addAttribute("listCount",noticePage.getTotalPages());
        model.addAttribute("currentPage",1);

        return "/board/notice";
    }

    // 검색 + 페이지
    @GetMapping("/search/{page}")
    public ResponseEntity<?> showNoticeByKeywordAndPage( NoticePageFormDto noticePageFormDto,
                                             @PathVariable int page, @RequestParam String keyword) {

        noticePageFormDto.setPage(page);

        Page<Notice> noticePage = noticeService.readNoticePage(noticePageFormDto);
        model.addAttribute("crud","selectKeyword");
        model.addAttribute("keyword",noticePageFormDto.getKeyword());
        model.addAttribute("type",noticePageFormDto.getType());

        model.addAttribute("noticeList",noticePage.getContent());
        model.addAttribute("listCount",noticePage.getTotalPages());
        model.addAttribute("currentPage",page);

        return "/board/notice";
       
    }

    
    //  공지사항 수정 페이지
    @GetMapping("/update")
    public ResponseEntity<?> update( @RequestParam Long id) {
        model.addAttribute("crud", "update");
        model.addAttribute("id", id);

        Notice notice = noticeService.readByIdNotice(id);
        model.addAttribute("notice", notice);
        return "/board/notice";
    }

   
    // 공지사항 수정
    @PutMapping("/update")
    public ResponseEntity<?> update(@Validated NoticeFormDto noticeFormDto) {
        noticeService.updateNotice(noticeFormDto);
        return "redirect:/notice";
    }

    // 공지사항 삭제
    @GetMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam Long id) {
        model.addAttribute("id", id);
        noticeService.deleteNotice(id);
        return "redirect:/notice";
    }
}
