package com.green.university.controller;

import com.green.university.dto.ScheduleDto;
import com.green.university.dto.ScheduleFormDto;
import com.green.university.exception.CustomRestfullException;
import com.green.university.entity.Schedule;
import com.green.university.config.security.CustomUserDetails;
import com.green.university.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author 편용림
 *
 */

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

	@Autowired
	private HttpSession session;

	@Autowired
	private ScheduleService scheduleService;

	/**
	 * 학사일정 페이지
	 *
	 */
	@GetMapping
	public ResponseEntity<?> schedule() { // 원본 기준 단순 상호작용 불가한 표 형식의 학사일정 보기

		// 학사 일정 전체 조회
		List<Schedule> schedules = scheduleService.readSchedule();
		return ResponseEntity.ok(Map.of(
                "schedules", schedules
        ));

	}

    // 원본 홈페이지 기준 학사정보 사이드 네비 - 학사일정 등록의 학사일정 리스트 단순 조회
    // crud의 상태에 따라 보기, 입력, 추가... 버튼 누르면 crud 파라미터 값이 변화함 (페이징처럼)
	@GetMapping("/list")
	public ResponseEntity<?> ScheduleList(@RequestParam(defaultValue = "select") String crud) {
		List<Schedule> schedules = scheduleService.readSchedule();

        return ResponseEntity.ok(Map.of(
                "schedules", schedules,
                "crud", crud
        ));
    }

	// 학사 일정 추가 crud=insert
	@PostMapping("/write")
	public ResponseEntity<?> ScheduleProc(@Valid ScheduleFormDto scheduleFormDto,
                                          @AuthenticationPrincipal CustomUserDetails principal) {

		Long staffId = principal.getId();
//		System.out.println("write");
//		System.out.println(scheduleFormDto);
		
		if (scheduleFormDto.getStartDay().equals("")){ // 값이 없을 때 처리
			throw new CustomRestfullException("날짜를 입력해주세요", HttpStatus.BAD_REQUEST, "/break/appList");
		}else if(scheduleFormDto.getEndDay().equals("")){
			throw new CustomRestfullException("날짜를 입력해주세요", HttpStatus.BAD_REQUEST, "/break/appList");
		}else if(scheduleFormDto.getInformation().equals("")){
			throw new CustomRestfullException("내용을 입력해주세요", HttpStatus.BAD_REQUEST, "/break/appList");
		}else {
            scheduleService.createSchedule(staffId, scheduleFormDto);
		}
		return ResponseEntity.ok().body("학사 일정 추가가 완료되었습니다.");
	}

    // 학사 일정 삭제 crud=delete
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteSchedule(@PathVariable("id") Long id) {
        scheduleService.deleteSchedule(id);

        return ResponseEntity.ok().body("학사 일정 삭제가 완료되었습니다.");
	}

    // 학사 일정 수정
    @PatchMapping("/update/{id}")
    public ResponseEntity<?> updateSchedule(@RequestBody ScheduleFormDto scheduleFormDto, @PathVariable("id") Long id) {
        scheduleService.updateSchedule(scheduleFormDto, id);

        return ResponseEntity.ok().body("학사 일정 수정이 완료되었습니다.");
    }

    // 선택한 학사 일정 상세 보기
	@GetMapping("/detail/{id}")
	public ResponseEntity<?> detailSchedule(@PathVariable("id") Long id, @RequestParam(defaultValue = "read") String crud) {
		ScheduleDto schedule = scheduleService.readScheduleById(id);
        return ResponseEntity.ok(Map.of(
                "schedule", schedule,
                "crud", crud
        ));
	}



}
