package com.green.university.global.utils;

import com.green.university.global.exception.CustomRestfullException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// 날짜, 시간 관련된 유틸
public class DateTimeUtil {

    // 날짜형 타입 관련 유틸
    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String dateTimeToString(LocalDateTime time) {
        if (time == null) return "";
        return time.format(DATE_TIME_FMT);
    }

    public static String dateToString(LocalDateTime time) {
        if (time == null) return "";
        return time.format(DATE_FMT);
    }

    // 날짜 기간 검증 (startDate가 endDate보다 늦으면 예외 발생)
    public static void validateDatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("시작 날짜와 종료 날짜는 필수입니다");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다");
        }
    }

}
