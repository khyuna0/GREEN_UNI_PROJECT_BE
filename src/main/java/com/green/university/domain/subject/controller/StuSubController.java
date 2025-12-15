package com.green.university.domain.subject.controller;

import com.green.university.domain.admin.service.SugangPeriodService;
import com.green.university.domain.admin.service.UserService;
import com.green.university.domain.breakapp.entity.BreakApp;
import com.green.university.domain.breakapp.service.BreakAppService;
import com.green.university.domain.student.dto.StuStatDto;
import com.green.university.domain.student.dto.StudentDto;
import com.green.university.domain.student.service.StuStatService;
import com.green.university.domain.subject.dto.CurrentSemesterSubjectSearchFormDto;
import com.green.university.domain.subject.dto.StuSubAppDto;
import com.green.university.domain.subject.dto.SubjectDto;
import com.green.university.domain.subject.dto.TimetableCourseDto;
import com.green.university.domain.subject.entity.PreStuSub;
import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.service.PreStuSubService;
import com.green.university.domain.subject.service.StuSubService;
import com.green.university.domain.subject.service.SubjectService;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.security.CustomUserDetails;
import com.green.university.global.utils.Define;
import com.green.university.global.utils.StuStatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// 수강 신청 관련 (preStuSub 포함) 강의 시간표는 SubjectController 대신 일부러 여기에 넣음
@RestController
@RequestMapping("/api/sugang")
public class StuSubController {

    @Autowired
    private SubjectService subjectService;
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
    // 수강신청 변경에 따라 학생의 예비 수강신청 내역이 둘(미완성, 완성)으로 나눠지게 만들어줌
    @PostMapping("/batch/move-pre-to-regular")
    public ResponseEntity<?> executeBatch() {
        stuSubService.movePreToStuSubBatch();
        return ResponseEntity.ok("배치 실행 완료");
    }

    @PostMapping("/batch/move-regular-to-detail")
    public ResponseEntity<?> executeBatch2() {
        stuSubService.moveStuSubToDetailBatch();
        return ResponseEntity.ok("배치 실행 완료2");
    }

    // ========================= 학생 기능 =========================
    // 🔥 수강 신청 탭에서 보여지는 전체 강의 시간표 조회 (현재 연도, 학기에 해당하는 강의 + 페이징 + 검색)
    @GetMapping("/subjectList")
    public ResponseEntity<?> sugangSubjectList(
            @ModelAttribute CurrentSemesterSubjectSearchFormDto dto,
            @PageableDefault(page = 0, size = Define.SUBJECT_PAGE_SIZE, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        // 현재 연도, 학기에 맞는 강의 목록
        Page<SubjectDto> subjectList = subjectService.readSubjectListByCurrentSemesterPage(dto, pageable);

        Map<String, Object> pagingResponse = new HashMap<>();
        pagingResponse.put("listCount", subjectList.getTotalElements());
        pagingResponse.put("totalPages", subjectList.getTotalPages());
        pagingResponse.put("currentPage", subjectList.getNumber());
        pagingResponse.put("lists", subjectList.getContent());

        return ResponseEntity.ok(pagingResponse);
    }

    // 🔥 예비 수강 신청 탭에서 보여지는 강의 목록 (현재 연도, 학기 강의 + 검색 + 페이징 + 수강신청 버튼 존재)
    @GetMapping("/presubjectlist")
    public ResponseEntity<?> sugangPreSubjectList(
            @ModelAttribute CurrentSemesterSubjectSearchFormDto dto,
            @PageableDefault(page = 0, size = Define.SUBJECT_PAGE_SIZE, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {

        // 예비 수강 신청 기간이 아니라면
        int currentStatus = sugangPeriodService.getCurrentStatus();
        if (currentStatus != 0) {
            throw new CustomRestfullException("예비 수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }
        Long studentId = principal.getId();

        // 현재 연도, 학기에 맞는 강의 목록
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

    // 🔥 수강 신청 탭에서 보여지는 강의 목록 (현재 연도, 학기 강의 + 검색 + 페이징 + 수강신청 버튼 존재)
    @GetMapping("/regularsubjectlist")
    public ResponseEntity<?> sugangSubjectList(
            @ModelAttribute CurrentSemesterSubjectSearchFormDto dto,
            @PageableDefault(page = 0, size = Define.SUBJECT_PAGE_SIZE, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails principal) {

        // 수강 신청 기간이 아니라면
        int currentStatus = sugangPeriodService.getCurrentStatus();
        if (currentStatus != 1) {
            throw new CustomRestfullException("수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }
        Long studentId = principal.getId();

        // 현재 연도, 학기에 맞는 강의 목록
        Page<SubjectDto> subjectPage = subjectService.readSubjectListByCurrentSemesterPage(dto, pageable);

        // 방법1. 학생의 수강 신청 여부(status)를 SubjectDto에 추가하는 것
        for (SubjectDto sub : subjectPage) {
            // 과목 id 가져오기
            Long subjectId = sub.getId();
            // 그 과목 id를 통해서 stusub에 신청 내역 확인하기
            Optional<StuSub> stuSub = stuSubService.readStuSub(studentId, subjectId);
            // 여기서 stusub가 true면 신청한거, false면 신청 안한 거 -> dto에 값 넣어주기
            sub.setStatus(stuSub.isPresent());
        }
        // 방법2. 각 강의마다 학생의 신청 상태(status) 업데이트 (깔끔 버전)
//        List<SubjectDto> subjectList = subjectPage.getContent().stream()
//                .map(subjectDto -> {
//                    // Optional을 사용해서 null-safe하게 처리
//                    Optional<StuSub> stuSubOpt = stuSubService.readStuSub(studentId, subjectDto.getId());
//                    // 존재하면 true, 없으면 false
//                    subjectDto.setStatus(stuSubOpt.isPresent());
//                    return subjectDto;
//                })
//                .toList();

        Map<String, Object> pagingResponse = new HashMap<>();
        pagingResponse.put("listCount", subjectPage.getTotalElements());
        pagingResponse.put("totalPages", subjectPage.getTotalPages());
        pagingResponse.put("currentPage", subjectPage.getNumber());
        pagingResponse.put("lists", subjectPage.getContent());

        return ResponseEntity.ok(pagingResponse);
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
        System.out.println("subjectId = " + subjectId);
        System.out.println("principal = " + principal.getId());
        stuSubService.createStuSub(principal.getId(), subjectId);
        preStuSubService.deleteBySubject_Id(subjectId);
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

    // 🔥 학생의 예비 / 수강 목록 조회 (기간에 따라 다르게)
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


    // 아마도 학생의 최종 수강 신청 내역 (아래 timetable 쓰면 될 듯..)
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

    // 최종 수강 신청 timetable 조회
    @GetMapping("/timetable")
    public ResponseEntity<?> getMyTimetable(@AuthenticationPrincipal CustomUserDetails principal) {

        // 예비 수강 신청 기간이면 최종 시간표 조회 불가
        int currentStatus = sugangPeriodService.getCurrentStatus();
        if (currentStatus == 0) {
            throw new CustomRestfullException(
                    "예비 수강 신청 기간에는 최종 시간표를 조회할 수 없습니다.",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 학적/휴학 상태 체크 (기존 stusublist/list와 동일한 흐름)
        Long studentId = principal.getId();

        StudentDto studentInfo = userService.readStudent(studentId);
        StuStatDto stuStatEntity = stuStatService.readCurrentStatus(studentInfo.getId());
        List<BreakApp> breakAppList = breakAppService.readByStudentId(studentInfo.getId());
        StuStatUtil.checkStuStat("수강신청", stuStatEntity, breakAppList);

        // 시간표 DTO 변환 결과 받아오기
        List<TimetableCourseDto> courses = stuSubService.readMyTimetable(studentId);

        // 총 학점 계산
        Long totalGrades = courses.stream()
                .mapToLong(TimetableCourseDto::getGrades)
                .sum();

        // 응답
        return ResponseEntity.ok(Map.of(
                "period", currentStatus,  // 1학기 or 2학기
                "courses", courses,
                "totalGrades", totalGrades
        ));
    }


}
