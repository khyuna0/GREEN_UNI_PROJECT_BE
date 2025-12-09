package com.green.university.controller;

import com.green.university.dto.CurrentSemesterSubjectSearchFormDto;
import com.green.university.dto.response.StuStatDto;
import com.green.university.dto.response.StuSubAppDto;
import com.green.university.dto.response.StudentDto;
import com.green.university.dto.response.SubjectDto;
import com.green.university.exception.CustomRestfullException;
import com.green.university.entity.*;
import com.green.university.config.security.CustomUserDetails;
import com.green.university.service.*;
import com.green.university.utils.Define;
import com.green.university.utils.StuStatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 서영
 * 수강 신청 관련 (preStuSub 포함) 강의 시간표는 SubjectController 대신 일부러 여기에 넣음
 */

@RestController
@RequestMapping("/api/sugang")
public class StuSubController {

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private CollegeService collegeService;
    @Autowired
    private PreStuSubService preStuSubService;
    @Autowired
    private StuSubService stuSubService;
    @Autowired
    private StuStatService stuStatService;
    @Autowired
    private BreakAppService breakAppService;
    @Autowired
    private UserService userService;
    @Autowired
    private SugangPeriodService sugangPeriodService;


    // ========================= 관리자 기능 =========================
    // 🔥 배치 실행용 엔드포인트 (관리자 전용)
    @PostMapping("/batch/move-pre-to-regular")
    public ResponseEntity<?> executeBatch() {
        stuSubService.movePreToStuSubBatch();
        return ResponseEntity.ok("배치 실행 완료");
    }

    // ========================= 학생 기능 =========================
    // 🔥 수강 신청에 사용할 강의 시간표 조회 (학생용) 현재 연도-학기에 해당하는 강의만 출력 + 페이징 처리 + 검색
    @GetMapping("/subjectList")
    public ResponseEntity<?> readSubjectList(
            @ModelAttribute CurrentSemesterSubjectSearchFormDto dto,
            @PageableDefault(page = 0, size = Define.SUBJECT_PAGE_SIZE, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        // 현재 학기에 맞는 강의 목록
        Page<SubjectDto> subjectList = subjectService.readSubjectListByCurrentSemesterPage(dto, pageable);

        Map<String, Object> pagingResponse = new HashMap<>();
        pagingResponse.put("listCount", subjectList.getTotalElements());
        pagingResponse.put("totalPages", subjectList.getTotalPages());
        pagingResponse.put("currentPage", subjectList.getNumber());
        pagingResponse.put("lists", subjectList.getContent());

        return ResponseEntity.ok(pagingResponse);
    }

    // 예비 수강 신청
    @GetMapping("/pre/{page}")
    public ResponseEntity<?> preStuSubApplication(@PathVariable("page") int page,
                                                  @AuthenticationPrincipal CustomUserDetails principal) { // page 값 int로 변경함

        // 예비 수강 신청 기간이 아니라면
        int currentStatus = sugangPeriodService.getCurrentStatus();
        if (currentStatus != 0) {
            throw new CustomRestfullException("예비 수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        // 이번 학기에 재학 상태가 되지 않는 학생이라면 진입 불가
        Long studentId = principal.getId();

        StudentDto studentInfo = userService.readStudent(studentId);

        StuStatDto stuStatEntity = stuStatService.readCurrentStatus(studentInfo.getId());
        List<BreakApp> breakAppList = breakAppService.readByStudentId(studentInfo.getId()); // 최근 순으로 정렬되어 있음

        StuStatUtil.checkStuStat("수강신청", stuStatEntity, breakAppList);

        // 강의 리스트
        List<SubjectDto> subjectList = subjectService.readSubjectListByCurrentSemester();
        Long subjectCount = (long) subjectList.size();

        // 총 페이지 수
        Long pageCount = (long) Math.ceil(subjectCount / 20.0);

        // 필터에 사용할 전체 학과 정보
        List<Department> deptList = collegeService.readDeptAll();

        // 필터에 사용할 강의 이름 정보 (중복 값 제거)
        List<String> subNameList = new ArrayList<>();
        for (SubjectDto subject : subjectList) {
            if (!subNameList.contains(subject.getName())) {
                subNameList.add(subject.getName());
            }
        }
        return ResponseEntity.ok(Map.of(
                "subjectCount", subjectCount,
                "pageCount", pageCount,
                "page", page,
                //"subjectList", subjectListLimit,
                "deptList", deptList,
                "subNameList", subNameList
        ));
    }

    // 🔥 예비 수강 신청 처리 (신청)
    @PostMapping("/pre/{subjectId}")
    public ResponseEntity<?> insertPreStuSubAppProc(@PathVariable Long subjectId,
                                                    @AuthenticationPrincipal CustomUserDetails principal) {
        // 예비 수강 신청 기간이 아니라면
        int currentStatus = sugangPeriodService.getCurrentStatus();
        if (currentStatus != 0) {
            throw new CustomRestfullException("예비 수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }
        preStuSubService.createPreStuSub(principal.getId(), subjectId);
        return ResponseEntity.ok().body("예비 수강 신청이 정상적으로 신청되었습니다.");
    }

    // 🔥 예비 수강 신청 처리 (취소)
    @DeleteMapping("/pre/{subjectId}")
    public ResponseEntity<?> deletePreStuSubAppProc(@PathVariable("subjectId") Long subjectId,
                                                    @AuthenticationPrincipal CustomUserDetails principal) {
        // 예비 수강 신청 기간이 아니라면
        int currentStatus = sugangPeriodService.getCurrentStatus();
        if (currentStatus != 0) {
            throw new CustomRestfullException("예비 수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }
        preStuSubService.deletePreStuSub(principal.getId(), subjectId);
        return ResponseEntity.ok().body("예비 수강 신청을 정상적으로 취소하였습니다.");

    }

    // 🔥 예비 수강 신청 탭에서 보여지는 강의 목록 (현재 연도, 학기 강의 + 검색 + 페이징 + 수강신청 버튼 존재)
    @GetMapping("/presubjectlist")
    public ResponseEntity<?> preStuSubApplicationSearch(
            @ModelAttribute CurrentSemesterSubjectSearchFormDto dto,
            @PageableDefault(page = 0, size = Define.SUBJECT_PAGE_SIZE, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {
        // 예비 수강 신청 기간이 아니라면
        int currentStatus = sugangPeriodService.getCurrentStatus();
        if (currentStatus != 0) {
            throw new CustomRestfullException("예비 수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }
        Long studentId = principal.getId();

        // 강의 리스트
        Page<SubjectDto> subjectList = subjectService.readSubjectListByCurrentSemesterPage(dto, pageable); // 현재 학기에 맞는 강의 목록

        // 학생이 예비 수강 신청을 했는지 여부 (status = false, true)
        for (SubjectDto sub : subjectList) {
            PreStuSub preStuSub = preStuSubService.readPreStuSub(studentId, sub.getId());
            sub.setStatus(preStuSub != null);
        }
        //
        Map<String, Object> pagingResponse = new HashMap<>();
        pagingResponse.put("listCount", subjectList.getTotalElements());
        pagingResponse.put("totalPages", subjectList.getTotalPages());
        pagingResponse.put("currentPage", subjectList.getNumber());
        pagingResponse.put("lists", subjectList.getContent());

        return ResponseEntity.ok(pagingResponse);
    }

    // 수강 신청
    @GetMapping("/application/{page}")
    public ResponseEntity<?> stuSubApplication(@PathVariable int page,
                                               @AuthenticationPrincipal CustomUserDetails principal) {

        // 수강 신청 기간이 아니라면
        int currentStatus = sugangPeriodService.getCurrentStatus();
        if (currentStatus != 1) {
            throw new CustomRestfullException("예비 수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        // 이번 학기에 재학 상태가 되지 않는 학생이라면 진입 불가
        Long studentId = principal.getId();

        StudentDto studentInfo = userService.readStudent(studentId);

        StuStatDto stuStatEntity = stuStatService.readCurrentStatus(studentInfo.getId());
        List<BreakApp> breakAppList = breakAppService.readByStudentId(studentInfo.getId()); // 최근 순으로 정렬되어 있음
        StuStatUtil.checkStuStat("수강신청", stuStatEntity, breakAppList);

        // 강의 리스트
        List<SubjectDto> subjectList = subjectService.readSubjectListByCurrentSemester();
        Long subjectCount = (long) subjectList.size();

        // 총 페이지 수
        Long pageCount = (long) Math.ceil(subjectCount / 20.0);

        // 필터에 사용할 전체 학과 정보
        List<Department> deptList = collegeService.readDeptAll();

        // 필터에 사용할 강의 이름 정보 (중복 값 제거)
        List<String> subNameList = new ArrayList<>();
        for (SubjectDto subject : subjectList) {
            if (!subNameList.contains(subject.getName())) {
                subNameList.add(subject.getName());
            }
        }

        return ResponseEntity.ok(Map.of(
                "subjectCount", subjectCount,
                "page", page,
                "pageCount", pageCount,
                //"subjectList", subjectListLimit,
                "deptList", deptList,
                "subNameList", subNameList
        ));
    }

    // 수강 신청 강의 목록에서 필터링
    @GetMapping("/application/search")
    public ResponseEntity<?> stuSubApplicationSearch(
            @Validated CurrentSemesterSubjectSearchFormDto currentSemesterSubjectSearchFormDto,
            @AuthenticationPrincipal CustomUserDetails principal) {

        // 수강 신청 기간이 아니라면
        int currentStatus = sugangPeriodService.getCurrentStatus();
        if (currentStatus != 1) {
            throw new CustomRestfullException("수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        Long studentId = principal.getId();

        // 강의 리스트
        List<SubjectDto> subjectList = subjectService
                .readSubjectListSearchByCurrentSemester(currentSemesterSubjectSearchFormDto);
        for (SubjectDto sub : subjectList) {
            // 현재 담겨 있는지 확인
            StuSub stuSub = stuSubService.readStuSub(studentId, sub.getId());
            if (stuSub != null) {
                sub.setStatus(true);
            } else {
                sub.setStatus(false);
            }
        }

        Long subjectCount = (long) subjectList.size();

        // 필터에 사용할 전체 학과 정보
        List<Department> deptList = collegeService.readDeptAll();

        // 필터에 사용할 강의 이름 정보 (중복 값 제거)
        List<String> subNameList = new ArrayList<>();
        for (SubjectDto subject : subjectList) {
            if (!subNameList.contains(subject.getName())) {
                subNameList.add(subject.getName());
            }
        }

        return ResponseEntity.ok(Map.of(
                "subjectCount", subjectCount,
                "subjectList", subjectList,
                "deptList", deptList,
                "subNameList", subNameList
        ));
    }

    // 🔥 수강 신청
    @PostMapping("/regular/{subjectId}")
    public ResponseEntity<?> addStuSub(@PathVariable("subjectId") Long subjectId,
                                       @AuthenticationPrincipal CustomUserDetails principal) {
        // 수강 신청 기간이 아니라면
        int currentStatus = sugangPeriodService.getCurrentStatus();
        if (currentStatus != 1) {
            throw new CustomRestfullException("수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }
        stuSubService.createStuSub(principal.getId(), subjectId);
        return ResponseEntity.ok().body("수강 신청이 정상적으로 처리되었습니다.");
    }

    // 🔥 수강 신청 취소
    @DeleteMapping("/regular/{subjectId}")
    public ResponseEntity<?> deleteStuSub(@PathVariable("subjectId") Long subjectId,
                                          @AuthenticationPrincipal CustomUserDetails principal) {

        // 수강 신청 기간이 아니라면
        int currentStatus = sugangPeriodService.getCurrentStatus();
        if (currentStatus != 1) {
            throw new CustomRestfullException("수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }
        stuSubService.deleteStuSub(principal.getId(), subjectId);
        return ResponseEntity.ok().body("수강 신청 취소가 정상적으로 처리되었습니다.");
    }

    // 🔥 학생의 예비/수강 목록 조회 (기간에 따라 다르게)
    @GetMapping("/stusublist")
    public ResponseEntity<?> getStudentSubList(@AuthenticationPrincipal CustomUserDetails principal) {
        // 이번 학기에 재학 상태가 되지 않는 학생이라면 진입 불가
        Long studentId = principal.getId();
        StudentDto studentInfo = userService.readStudent(studentId);
        StuStatDto stuStatEntity = stuStatService.readCurrentStatus(studentInfo.getId());
        List<BreakApp> breakAppList = breakAppService.readByStudentId(studentInfo.getId()); // 최근 순으로 정렬되어 있음
        StuStatUtil.checkStuStat("수강신청", stuStatEntity, breakAppList);

        // 예비 수강 신청 기간 (type == 0)
        int currentStatus = sugangPeriodService.getCurrentStatus();
        if (currentStatus == 0) {
            List<StuSubAppDto> preStuSubList = preStuSubService.readPreStuSubList(studentId);
            Long totalGrades = preStuSubList.stream()
                    .mapToLong(StuSubAppDto::getGrades)
                    .sum();

            System.out.println("[예비] 강의:" + preStuSubList);
            System.out.println("[예비] 학점:" + totalGrades);

            return ResponseEntity.ok(Map.of(
                    "period", 0,
                    "preStuSubList", preStuSubList,
                    "totalGrades", totalGrades
            ));
        }

        // (예비 수강 신청 목록에서 수강 신청으로 자동으로 넘어간 강의와, 직접 신청해야 하는 강의를 분리해서 보여줄것)
        // 수강 신청 기간 (type == 1)
        if (currentStatus == 1) {
            List<StuSubAppDto> preStuSubList = stuSubService.readPreStuSubByStuSub(studentId); // 미완료
            List<StuSubAppDto> stuSubList = stuSubService.readStuSubList(studentId); // 완료
            Long totalGrades = stuSubList.stream()
                    .mapToLong(StuSubAppDto::getGrades)
                    .sum();
            System.out.println("[수강] 미완료" + stuSubList);
            System.out.println("[수강] 강의:" + preStuSubList);
            System.out.println("[수강] 학점:" + totalGrades);

            return ResponseEntity.ok(Map.of(
                    "period", 1,
                    "preStuSubList", preStuSubList, // 미완료 강의
                    "stuSubList", stuSubList, // 완료 강의
                    "totalGrades", totalGrades
            ));
        }

        // 종료 (최종 수강 신청 목록만)
        List<StuSubAppDto> stuList = stuSubService.readStuSubList(studentId);
        Long totalGrades = stuList.stream().mapToLong(StuSubAppDto::getGrades).sum();

        return ResponseEntity.ok(Map.of(
                "period", 2,
                "stuSubList", stuList,
                "totalGrades", totalGrades
        ));
    }


    // 아마도 학생의 최종 수강 신청 내역!
    @GetMapping("/list")
    public ResponseEntity<?> stuSubAppList(@AuthenticationPrincipal CustomUserDetails principal) {

        // 예비 수강 신청 기간이라면
        int currentStatus = sugangPeriodService.getCurrentStatus();
        if (currentStatus == 0) {
            throw new CustomRestfullException("예비 수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        // 이번 학기에 재학 상태가 되지 않는 학생이라면 진입 불가
        // 해당 학생의 학적 상태가 '졸업' 또는 '자퇴'라면 X
        // 해당 학생이 이번 학기 휴학을 승인받은 상태라면 X
        Long studentId = principal.getId();

        StudentDto studentInfo = userService.readStudent(studentId);

        StuStatDto stuStatEntity = stuStatService.readCurrentStatus(studentInfo.getId());
        List<BreakApp> breakAppList = breakAppService.readByStudentId(studentInfo.getId()); // 최근 순으로 정렬되어 있음
        StuStatUtil.checkStuStat("수강신청", stuStatEntity, breakAppList);

        List<StuSubAppDto> stuSubList = stuSubService.readStuSubList(studentId);

        Long totalGrades = 0L;
        for (StuSubAppDto s : stuSubList) {
            totalGrades += s.getGrades();
        }

        return ResponseEntity.ok(Map.of(
                "stuSubList", stuSubList,
                "totalGrades", totalGrades
        ));
    }

}
