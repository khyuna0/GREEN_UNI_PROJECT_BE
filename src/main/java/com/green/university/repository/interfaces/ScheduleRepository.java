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

}
