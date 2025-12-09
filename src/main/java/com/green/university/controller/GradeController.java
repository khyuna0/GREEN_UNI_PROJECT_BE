package com.green.university.controller;

import com.green.university.dto.response.GradeDto;
import com.green.university.dto.response.MyGradeDto;
import com.green.university.config.security.CustomUserDetails;
import com.green.university.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

// 금학기,학기별 성적, 누계성적 조회
@RestController
@RequestMapping("/api/grade")
public class GradeController {

    @Autowired
    private HttpSession session;

    @Autowired
    private GradeService gradeService;

    /**
     * 금학기 성적조회
     *
     * @return
     */
    @GetMapping("/thisSemester")
    public ResponseEntity<?> thisSemester(@AuthenticationPrincipal CustomUserDetails principal) {

        Long studentId = principal.getId();

        // 학생이 수강 신청한 연도 조회
        List<GradeDto> yearList = gradeService.readGradeYearByStudentId(studentId);

        List<GradeDto> thisSemester = null; // 값 보내주기 위해 선언부만 만듬
        MyGradeDto mygrade = null;

        // 수강한 연도가 없으면 금학기 성적조회 x
        if (!yearList.isEmpty()) {

            // 금학기 성적조회 기능
            thisSemester = gradeService.readThisSemesterByStudentId(studentId);

            // 누계 성적 조회
            mygrade = gradeService.readMyGradeByStudentId(studentId);
        }

        return ResponseEntity.ok(Map.of(
                "yearList", yearList,
                "gradeList", thisSemester, // 상태에 따라 null 일수도 있음
                "mygrade", mygrade
        ));
    }

    /**
     * 학기별 성적조회 (초기 + 필터 통합)
     */
    @GetMapping("/semester")
    public ResponseEntity<?> semester(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(value = "type", required = false, defaultValue = "전체") String type,
            @RequestParam(value = "subyear", required = false) Long subYear,
            @RequestParam(value = "semester", required = false) Long semester
    ) {
        Long studentId = principal.getId();

        List<GradeDto> yearList = gradeService.readGradeYearByStudentId(studentId);
        List<GradeDto> semesterList = gradeService.readGradeSemesterByStudentId(studentId);

        List<GradeDto> gradeList;

        // 초기(필터 미선택)
        if (subYear == null || semester == null) {
            gradeList = gradeService.readAllGradeByStudentId(studentId);
        } else {
            // 필터 조회
            if ("전체".equals(type)) {
                gradeList = gradeService.readGradeByStudentId(studentId, subYear, semester);
            } else {
                gradeList = gradeService.readGradeByType(studentId, subYear, semester, type);
            }
        }

        return ResponseEntity.ok(Map.of(
                "yearList", yearList,
                "semesterList", semesterList,
                "gradeList", gradeList
        ));
    }

    /**
     * 총 누계성적 조회
     *
     * @return
     */
    @GetMapping("/total")
    public ResponseEntity<?> totalGrade( @AuthenticationPrincipal CustomUserDetails principal) {

        Long studentId = principal.getId();

        // 학생이 수강 신청한 연도 조회
        List<GradeDto> yearList = gradeService.readGradeYearByStudentId(studentId);
        List<MyGradeDto> gradeList = gradeService.readgradeinquiryList(studentId);

        return ResponseEntity.ok(Map.of(
                "yearList", yearList,
                "gradeList", gradeList
        ));
    }

}
