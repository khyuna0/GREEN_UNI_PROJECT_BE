package com.green.university.global.utils;

import java.time.LocalDate;
import java.time.ZoneId;

public class TermUtil {
    // 지금날짜로 학기계산

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static long currentYear() {
        LocalDate now = LocalDate.now(KST);
        int m = now.getMonthValue();
        return (m <= 2) ? now.getYear() - 1 : now.getYear();
    }

    public static long currentSemester() {
        LocalDate now = LocalDate.now(KST);
        int m = now.getMonthValue();
        return (m >= 3 && m <= 8) ? 1L : 2L;
    }
}
