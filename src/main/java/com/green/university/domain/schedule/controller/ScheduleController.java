package com.green.university.domain.schedule.controller;

import com.green.university.domain.schedule.dto.ScheduleDto;
import com.green.university.domain.schedule.dto.ScheduleFormDto;
import com.green.university.domain.schedule.entity.Schedule;
import com.green.university.domain.schedule.service.ScheduleService;
import com.green.university.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    // 학사일정 페이지
    @GetMapping
    public ResponseEntity<?> schedule() {
        // 학사 일정 전체 조회
        List<Schedule> schedules = scheduleService.readSchedule();
        return ResponseEntity.ok(Map.of(
                "schedules", schedules
        ));

    }

    // 학사 일정 추가
    @PostMapping("/write")
    public ResponseEntity<?> ScheduleProc(@Valid @RequestBody ScheduleFormDto scheduleFormDto,
                                          @AuthenticationPrincipal CustomUserDetails principal) {

        Long staffId = principal.getId();
        scheduleService.createSchedule(staffId, scheduleFormDto);
        return ResponseEntity.ok().body("학사 일정 추가가 완료되었습니다.");
    }

    // 학사 일정 삭제
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
    public ResponseEntity<?> detailSchedule(@PathVariable("id") Long id) {
        ScheduleDto schedule = scheduleService.readScheduleById(id);
        return ResponseEntity.ok(Map.of(
                "schedule", schedule
        ));
    }


}
