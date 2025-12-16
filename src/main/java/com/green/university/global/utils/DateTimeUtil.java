package com.green.university.global.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// 날짜형 타입 관련 유틸
public class DateTimeUtil {

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

}
