package com.green.university.domain.schedule.repository;

import com.green.university.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 월별 학사일정 조회
    List<Schedule> findByStartDayBetween(LocalDate start, LocalDate end);

}
