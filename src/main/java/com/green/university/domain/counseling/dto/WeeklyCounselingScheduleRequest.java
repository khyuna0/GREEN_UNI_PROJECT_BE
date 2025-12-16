package com.green.university.domain.counseling.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class WeeklyCounselingScheduleRequest {

    private LocalDate weekStartDate;              // 이번 주 월요일
    private Map<LocalDate, List<Long>> slots;     // 날짜별 시작 시간
    private Long subYear;
    private Long semester;
}
