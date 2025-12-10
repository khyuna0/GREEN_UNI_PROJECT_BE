package com.green.university.repository;

import com.green.university.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 월별 학사일정 조회
    List<Schedule> findByStartDayBetween(LocalDate start, LocalDate end);

}
