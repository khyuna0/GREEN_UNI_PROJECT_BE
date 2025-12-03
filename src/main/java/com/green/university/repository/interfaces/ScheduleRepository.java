package com.green.university.repository.interfaces;

import com.green.university.dto.ScheduleDto;
import com.green.university.dto.ScheduleFormDto;
import com.green.university.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDate;
import java.util.List;


public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 월별 학사일정 조회
    public List<Schedule> findByStartDayBetween(LocalDate start, LocalDate end);

    // 시작일(startDay)이 특정 범위(두 날짜 사이)에 포함되는 데이터 조회
    // 서비스에서 아래 방식으로 처리함
    //         LocalDate start = LocalDate.of(year, month, 1);
    //        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
}
