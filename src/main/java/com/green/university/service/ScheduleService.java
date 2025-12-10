package com.green.university.service;

import com.green.university.dto.ScheduleDto;
import com.green.university.dto.ScheduleFormDto;
import com.green.university.exception.CustomRestfullException;
import com.green.university.repository.ScheduleRepository;
import com.green.university.entity.Schedule;
import com.green.university.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleService {

	@Autowired
	private ScheduleRepository scheduleRepository;

    @Autowired
    private StaffRepository staffRepository;

	// 학사일정 조회
	public List<Schedule> readSchedule() {
		List<Schedule> schedule = scheduleRepository.findAll();
		return schedule;
	}

	// 학사일정 조회 (디테일)
	public ScheduleDto readScheduleById(Long id) {
		Schedule schedule = scheduleRepository.findById(id).orElseThrow(() -> new CustomRestfullException("없는 학사일정입니다", HttpStatus.NOT_FOUND));

		return new ScheduleDto(schedule);
	}

	// 학사일정 추가
	@Transactional
	public void createSchedule(Long staffId, ScheduleFormDto dto) {
		Schedule schedule = new Schedule();
		schedule.setStaff(staffRepository.findById(staffId).orElseThrow());
		schedule.setStartDay(dto.getStartDay());
		schedule.setEndDay(dto.getEndDay());
		schedule.setInformation(dto.getInformation());

        scheduleRepository.save(schedule);

	}

	// 학사일정 수정
	@Transactional
	public void updateSchedule(ScheduleFormDto dto, Long id) {

        //학사일정 아이디로 찾아 수정하기
        Schedule schedule = scheduleRepository.findById(id).orElseThrow(() -> new CustomRestfullException("해당 학사일정 없음", HttpStatus.NOT_FOUND));
        schedule.setStartDay(dto.getStartDay());
        schedule.setEndDay(dto.getEndDay());
        schedule.setInformation(dto.getInformation());

        scheduleRepository.save(schedule);

	}

	// 학사일정 삭제
	@Transactional
	public void deleteSchedule(Long id) {

		 scheduleRepository.deleteById(id);

	}

//	// 학사일정 월에 있는 일정 조회
//	@Transactional
//	public List<ScheduleDto> readScheduleDto() {
//
//		List<ScheduleDto> scheduleDto = scheduleRepository.selectSchoduleMouth();
//		return scheduleDto;
//	}
	
	// 학생이 본인 월별 학사일정 조회
    @Transactional
    public List<Schedule> readScheduleListByMonth(int year, int month) {

        // 해당 연도·월의 1일
        LocalDate start = LocalDate.of(year, month, 1);
        // 해당 연도·월의 마지막 날 (28~31일)
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return scheduleRepository.findByStartDayBetween(start, end);
    }

}
