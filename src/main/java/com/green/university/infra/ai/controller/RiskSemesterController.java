package com.green.university.infra.ai.controller;

import com.green.university.infra.ai.SemesterFinalizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/risks/semester")
public class RiskSemesterController {

    private final SemesterFinalizeService semesterFinalizeService;

    @PostMapping("/finalize")
    public ResponseEntity<?> finalizeSemester(
            @RequestParam Long year,
            @RequestParam Long semester,
            @RequestParam(required = false) Long studentId
    ) {
        int processed = semesterFinalizeService.finalizeSemester(year, semester, studentId);

        return ResponseEntity.ok().body(
                java.util.Map.of(
                        "year", year,
                        "semester", semester,
                        "studentId", studentId,
                        "processedStudents", processed,
                        "message", "학기 누계 위험 평가 요청 완료 (AI 분석은 비동기로 진행됨)"
                )
        );
    }
}
