package com.green.university.domain.admin.controller;

import com.green.university.domain.admin.entity.SugangPeriodStatus;
import com.green.university.domain.admin.service.SugangPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sugangperiod")
@PreAuthorize("hasRole('STAFF')")
public class SugangPeriodController {

    @Autowired
    private SugangPeriodService service;

    // 현재 상태 조회
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPeriod() {
        int status = service.getCurrentStatus();
        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("description", SugangPeriodStatus.fromCode(status).getDescription());
        return ResponseEntity.ok(response);
    }

    // 상태 업데이트 (관리자만)
    @PutMapping("/update")
    public ResponseEntity<String> updatePeriod(@RequestBody Map<String, Integer> request) {
        int newStatus = request.get("status");
        service.updateStatus(newStatus);
        return ResponseEntity.ok("수강신청 기간이 변경되었습니다.");
    }
}
