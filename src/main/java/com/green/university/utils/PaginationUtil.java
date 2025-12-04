package com.green.university.utils;

import org.springframework.data.domain.Page;

public class PaginationUtil {

    // pageBlockSize = 페이지 블록 크기 예 1 ~ 10
    public static <T> PaginationResult build(Page<T> pageData, int currentPage, int pageBlockSize) {

        int totalPage = pageData.getTotalPages();
        if (totalPage == 0) totalPage = 1; // 데이터 없는 경우

        // 페이지 블록 계산 (1부터 시작)
        int startPage = ((currentPage - 1) / pageBlockSize) * pageBlockSize + 1;
        int endPage = Math.min(startPage + pageBlockSize - 1, totalPage);

        return new PaginationResult(
                currentPage,                       // 현재 페이지
                totalPage,                         // 총 페이지 수
                startPage,                         // 블록 시작 페이지
                endPage,                           // 블록 끝 페이지
                (int) pageData.getTotalElements()  // 전체 데이터 수
        );
    }

    // 페이지네이션 결과를 담는 전용 DTO (데이터 묶음 객체)
    public record PaginationResult(
            int currentPage,
            int totalPage,
            int startPage,
            int endPage,
            int totalElements
    ) {}
}
