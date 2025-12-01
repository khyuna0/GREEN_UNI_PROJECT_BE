package com.green.university.repository.interfaces;

import com.green.university.dto.ScheduleDto;
import com.green.university.dto.ScheduleFormDto;
import com.green.university.repository.model.Schedule;


import java.util.List;


public interface ScheuleRepository { // todo ScheduleRepository로 변경

// 기본 기능
public Long insertSchoeduleFormDto(Schedule schedule);
public Long updateSchoeduleFormDtoBycontent(ScheduleFormDto scheduleFormDto);
public Long deleteSchoeduleFormDtoByStaffIdAndId(Long id);
public List<Schedule> selectSchodule();
public List<ScheduleDto> selectSchoduleMouth();


// 학사일정 조회 (디테일)
public ScheduleDto selectScheduleById(Long id);

// 월별 학사일정 조회
public List<Schedule> selectListByMonth(Long month);

}
