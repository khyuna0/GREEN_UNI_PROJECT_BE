package com.green.university.repository.interfaces;

import com.green.university.dto.NoticeFormDto;
import com.green.university.dto.NoticePageFormDto;
import com.green.university.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

/*
 *  박성희
 *  공지 repository
 */


public interface NoticeRepository extends JpaRepository<Notice,Long> {
	public Long insert(NoticeFormDto noticeFormDto);
	public List<Notice> selectByNoticeDto(NoticePageFormDto noticePageFormDto);
	public Notice selectById(Long id);
	public Long updateByNoticeDto(NoticeFormDto noticeFormDto);

	
	// 파일
	public Long insertFile(NoticeFormDto noticeFormDto);
	public Long selectLimit(NoticeFormDto noticeFormDto);
	
	// 페이징
	public List<Notice> selectByNoticeDtoOrderBy();
	public Long selectNoticeCount(NoticePageFormDto noticePageFormDto);
	
	// 검색
	public List<Notice> selectNoticeByKeyword(NoticePageFormDto noticePageFormDto);
	public List<Notice> selectNoticeByTitle(NoticePageFormDto noticePageFormDto);
	public Long selectNoticeCountByTitle(NoticePageFormDto noticePageFormDto);
	public Long selectNoticeCountByKeyword(NoticePageFormDto noticePageFormDto);
	
	// 조회수
	public Long updateViews(Long id);
	
	// 메인 페이지에 사용할 최신글 5개
	public List<NoticeFormDto> selectLimit5();
}
