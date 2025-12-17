package com.green.university.domain.breakapp.controller;

import com.green.university.domain.admin.service.UserService;
import com.green.university.domain.breakapp.dto.BreakAppFormDto;
import com.green.university.domain.breakapp.entity.BreakApp;
import com.green.university.domain.breakapp.service.BreakAppService;
import com.green.university.domain.student.dto.StudentDto;
import com.green.university.domain.student.service.StuStatService;
import com.green.university.domain.university.service.CollegeService;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.security.CustomUserDetails;
import com.green.university.global.utils.Define;
import com.green.university.global.utils.TermUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author 서영
 * 휴학 신청 관련 컨트롤러
 */
@RestController
@RequestMapping("/api/break")
public class BreakAppController {

    @Autowired
    private HttpSession session;

    @Autowired
    private BreakAppService breakAppService;

    @Autowired
    private StuStatService stuStatService;

    @Autowired
    private UserService userService;

    @Autowired
    private CollegeService collegeService;

    //휴학 신청 페이지
    @GetMapping("/application")
    public ResponseEntity<?> breakApplication(@AuthenticationPrincipal CustomUserDetails principal) {

        // JWT 에서 꺼낸 현재 로그인 학생 ID
        Long studentId = principal.getId();
        StudentDto studentInfo = userService.readStudent(studentId);

        // 학과 이름
        String deptName = collegeService.readDeptById(studentInfo.getDepartment().getId()).getName();

        // 단과대 이름
        String collName = collegeService
                .readCollById(collegeService.readDeptById(studentInfo.getDepartment().getId()).getCollege().getId()).getName();

        // 학생이 재학 상태가 아니라면 신청 불가능
        if (!stuStatService.readCurrentStatus(principal.getId()).getStatus().equals("재학")) {
            throw new CustomRestfullException("휴학 신청 대상이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        List<BreakApp> breakList = breakAppService.readByStudentId(principal.getId());
        // 이미 이번 학기 신청 내역이 있다면 신청 불가능 (반려되지 않았다면)
        if (!breakList.isEmpty()) {
            // 숫자 객체를 == 이 아닌 equals 로 비교하게 변경 -> Long 써서 그런듯
            if (Objects.equals(breakList.get(0).getFromYear(), TermUtil.currentYear())
                    && Objects.equals(breakList.get(0).getFromSemester(), TermUtil.currentSemester())
                    && !breakList.get(0).getStatus().equals("반려")) {
                throw new CustomRestfullException("이미 휴학 신청 내역이 존재합니다.", HttpStatus.BAD_REQUEST);
            }
        }

        return ResponseEntity.ok(Map.of(
                "student", studentInfo,
                "deptName", deptName,
                "collName", collName,
                "currentYear", TermUtil.currentYear(),          // ✅ 변경: 추가
                "currentSemester", TermUtil.currentSemester()   // ✅ 변경: 추가
        ));
    }

    /**
     * 휴복학 신청 (신청하면 교직원이 확인해서 승인하면 학적 변동)
     *
     * @return 휴복학 신청 내역 페이지
     */
    @PostMapping("/application")
    public ResponseEntity<?> breakApplicationProc(@Validated @RequestBody BreakAppFormDto breakAppFormDto,
                                                  @AuthenticationPrincipal CustomUserDetails principal) {

        Long studentId = principal.getId();

        // 시작(현재) 연도/학기
        long fromYear = TermUtil.currentYear();
        long fromSem  = TermUtil.currentSemester();

        // 종료(사용자 선택) 연도/학기  (BreakAppFormDto가 Long이면 언박싱)
        Long toYearObj = breakAppFormDto.getToYear();
        Long toSemObj  = breakAppFormDto.getToSemester();

        if (toYearObj == null || toSemObj == null) {
            throw new CustomRestfullException("종료 연도/학기를 입력해주세요.", HttpStatus.BAD_REQUEST);
        }


        long toYear = toYearObj;
        long toSem  = toSemObj;

        // 종료가 시작보다 이전이면 신청 불가
        if (toYear < fromYear || (toYear == fromYear && toSem < fromSem)) {
            throw new CustomRestfullException("종료 학기가 시작 학기 이전입니다.", HttpStatus.BAD_REQUEST);
        }

        // 선택한 종료 연도-학기가 시작 연도-학기보다 이전이라면 신청 불가능
        // ex) 시작 연도-학기 : 2023-2 / 종료 연도-학기 2023-1
//        if (TermUtil.currentYear().equals(breakAppFormDto.getToYear())  // 숫자 객체를 == 이 아닌 equals 로 비교하게 변경
//                && TermUtil.currentSemester() > breakAppFormDto.getToSemester()) {
//            throw new CustomRestfullException("종료 학기가 시작 학기 이전입니다.", HttpStatus.BAD_REQUEST);
//        }

        breakAppFormDto.setStudentId(studentId);
        breakAppFormDto.setFromYear(fromYear);
        breakAppFormDto.setFromSemester(fromSem);

        breakAppService.createBreakApp(breakAppFormDto);
        return ResponseEntity.ok().body("휴복학 신청이 정상적으로 처리되었습니다.");

    }

    /**
     * @return 휴복학 신청 내역 페이지 (학생용)
     */
    @GetMapping("/list")
    public ResponseEntity<?> breakAppListByStudentId(@AuthenticationPrincipal CustomUserDetails principal) {

        Long studentId = principal.getId();

        List<BreakApp> breakAppList = breakAppService.readByStudentId(studentId);

        return ResponseEntity.ok(Map.of(
                "breakAppList", breakAppList
        ));
    }

    /**
     * @return 처리되지 않은 휴복학 신청 내역 페이지 (교직원용)
     */
    @GetMapping("/list/staff")
    public ResponseEntity<?> breakAppListByState() {

        List<BreakApp> breakAppList = breakAppService.readByStatus("처리중");

        return ResponseEntity.ok(Map.of(
                "breakAppList", breakAppList
        ));
    }

    /**
     * @return 휴학 신청서 확인 학생 / 교직원에 따라 옆에 카테고리 바뀌어야 함
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> breakDetail(@PathVariable("id") Long id) {

        // 휴복학 신청
        BreakApp breakApp = breakAppService.readById(id);

        // 신청한 학생
        StudentDto studentInfo = userService.readStudent(breakApp.getStudent().getId());

        // 학과 이름
        String deptName = collegeService.readDeptById(studentInfo.getDepartment().getId()).getName();

        // 단과대 이름
        String collName = collegeService
                .readCollById(collegeService.readDeptById(studentInfo.getDepartment().getId()).getCollege().getId()).getName();

        return ResponseEntity.ok(Map.of(
                "breakApp", breakApp,
                "student", studentInfo,
                "deptName", deptName,
                "collName", collName
        ));
    }

    /**
     * 휴학 신청 취소 (학생)
     */
    @PostMapping("/delete/{id}")
    public ResponseEntity<?> deleteBreakApp(@PathVariable("id") Long id,
                                            @AuthenticationPrincipal CustomUserDetails principal) {

        Long studentId = principal.getId();

        // 신청서의 학번과 현재 로그인된 아이디가 일치하는지 확인
        if (!breakAppService.readById(id).getStudent().getId().equals(studentId)) {
            throw new CustomRestfullException("해당 신청자만 신청을 취소할 수 있습니다.", HttpStatus.UNAUTHORIZED);
        }

        breakAppService.deleteById(id);

        return ResponseEntity.ok().body("휴학 신청 취소가 정상적으로 처리되었습니다.");

    }

    /**
     * 휴학 신청 처리 (교직원)
     */
    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateBreakApp(@PathVariable("id") Long id, String status) {

        breakAppService.updateById(id, status);

        return ResponseEntity.ok().body("휴학 신청 처리가 정상적으로 처리되었습니다.");
    }

}
