package com.green.university.service;

import com.green.university.dto.ScheduleDto;
import com.green.university.dto.ScheduleFormDto;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.repository.interfaces.ScheuleRepository;
import com.green.university.entity.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ScheuleService { // todo ScheduleService로 변경

	@Autowired
	private ScheuleRepository scheuleRepository;

	// 학사일정 조회
	public List<Schedule> readSchedule() {
		List<Schedule> schedule = scheuleRepository.selectSchodule();
		return schedule;
	}

	// 학사일정 조회 (디테일)
	public ScheduleDto readScheduleById(Long id) {
		ScheduleDto schedule = scheuleRepository.selectScheduleById(id);
		return schedule;
	}

	// 학사일정 추가
	@Transactional
	public void createSchedule(Long staffId, ScheduleFormDto dto) {
		Schedule schedule = new Schedule();
		schedule.setStaffId(staffId);
		schedule.setStartDay(dto.getStartDay());
		schedule.setEndDay(dto.getEndDay());
		schedule.setInformation(dto.getInformation());

		Long resultRowCount = scheuleRepository.insertSchoeduleFormDto(schedule);
		if(resultRowCount != 1) {
			throw new CustomRestfullException("요청을 처리하지 못했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}		
	}

	// 학사일정 업데이트
	@Transactional
	public Long updateSchedule(ScheduleFormDto scheduleFormDto) {
		

		Long resultRowCount = scheuleRepository.updateSchoeduleFormDtoBycontent(scheduleFormDto);
		
		return resultRowCount;

	}

	// 학사일정 삭제
	@Transactional
	public Long deleteSchedule(Long id) {

		Long resultRowCount = scheuleRepository.deleteSchoeduleFormDtoByStaffIdAndId(id);

		return resultRowCount;
	}

	// 학사일정 월에 있는 일정 조회
	@Transactional
	public List<ScheduleDto> readScheduleDto() {

		List<ScheduleDto> scheduleDto = scheuleRepository.selectSchoduleMouth();
		return scheduleDto;
	}
	
	// 월별 학사일정 조회
	@Transactional
	public List<Schedule> readScheduleListByMonth(Long month) {
		
		List<Schedule> scheduleList = scheuleRepository.selectListByMonth(month);
		return scheduleList;
	}
}
