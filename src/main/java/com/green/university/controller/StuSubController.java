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

    /**
     * 현재 수강 신청 기간 상태 확인, 수강 신청 기간 상태 변경
     */

    // 예비 수강신청 기간 : 0, 수강신청 기간 : 1, 수강신청 기간 종료 : 2
    public static Long SUGANG_PERIOD = 0L;

    // 지금 SUGANG_PERIOD 상태 확인
    @GetMapping("/period")
    public ResponseEntity<?> getPeriodStatus() {

        return ResponseEntity.ok(Map.of("period", SUGANG_PERIOD));
    }

    // ========= 수강 신청 기간 상태 변경 (버튼이든 뭐든... 호출 해서 변경)

    // 예비 수강 신청 기간 -> 수강 신청 기간으로 상태 변경
    @PostMapping("/updatePeriod1")
    public ResponseEntity<?> updatePeriodProc1() {
        SUGANG_PERIOD = 1L;

        stuSubService.createStuSubByPreStuSub();

        return ResponseEntity.ok(Map.of("period", SUGANG_PERIOD));
    }

    // 수강 신청 기간 -> 종료로 변경
    @GetMapping("/updatePeriod2")
    public ResponseEntity<?> updatePeriodProc2() {
        SUGANG_PERIOD = 2L;

        return ResponseEntity.ok(Map.of("period", SUGANG_PERIOD));
    }


    /**
     * 과목 조회 및 검색 (페이징)
     */


    // 🖋 수강 신청에 사용할 강의 정보 (학생용) 현재 연도-학기에 해당하는 강의만 출력 + 페이징 처리 + 검색
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

    // 과목 조회 (현재 학기)에서 필터링
    @GetMapping("/subjectList/search")
    public ResponseEntity<?> readSubjectListSearch(@Validated CurrentSemesterSubjectSearchFormDto currentSemesterSubjectSearchFormDto) {

        // 프론트에서 매개변수 DTO는
        //    api.get("/sugang/subjectList/search", {
//        params: {
//            type: selectedType,
//                    deptId: selectedDept,
//                    name: searchName,
//                    subYear: 2025,
//                    semester: 1,
//                    page: 1
//        }
//    }); -> 이 형식으로 채워줘야 한다.


        // 강의 리스트
        List<SubjectDto> subjectList = subjectService
                .readSubjectListSearchByCurrentSemester(currentSemesterSubjectSearchFormDto);

        Long subjectCount = (long) subjectList.size();

        // 필터에 사용할 전체 학과 정보
        List<Department> deptList = collegeService.readDeptAll();

        // 필터에 사용할 강의 이름 정보 (중복 값 제거)
        List<String> subNameList = new ArrayList<>();
        for (SubjectDto subject : subjectService.readSubjectListByCurrentSemester()) {
            if (!subNameList.contains(subject.getName())) {
                subNameList.add(subject.getName());
            }
        }

        return ResponseEntity.ok(Map.of(
                "subjectList", subjectList,
                "subjectCount", subjectCount,
                "deptList", deptList,
                "subNameList", subNameList
        ));
    }

    /**
     * 예비 수강 신청
     */

    @GetMapping("/pre/{page}")
    public ResponseEntity<?> preStuSubApplication(@PathVariable("page") int page,
                                                  @AuthenticationPrincipal CustomUserDetails principal) { // page 값 int로 변경함

        // 예비 수강 신청 기간이 아니라면
        if (SUGANG_PERIOD != 0) {
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

        // 현재 페이지 Todo 페이지 타입 처리하기
//        Page<SubjectDto> subjectListLimit = subjectService.readSubjectListByCurrentSemesterPage((page - 1) * 20);
//        for (SubjectDto sub : subjectListLimit) {
//            // 현재 담겨 있는지 확인
//            PreStuSub preStuSub = preStuSubService.readPreStuSub(principal.getId(), sub.getId());
//            if (preStuSub != null) {
//                sub.setStatus(true);
//            } else {
//                sub.setStatus(false);
//            }
//        }

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

    // 🖋 예비 수강 신청 처리 (신청)
    @PostMapping("/pre/{subjectId}")
    public ResponseEntity<?> insertPreStuSubAppProc(@PathVariable Long subjectId,
                                                    @AuthenticationPrincipal CustomUserDetails principal) {
        // 예비 수강 신청 기간이 아니라면 (개발 중이라 잠시 주석처리)
//        if (SUGANG_PERIOD != 0) {
//            throw new CustomRestfullException("예비 수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
//        }
        Long studentId = principal.getId();
        preStuSubService.createPreStuSub(studentId, subjectId);
        return ResponseEntity.ok().body("수강 신청이 정상적으로 처리되었습니다.");
    }

    // 예비 수강 신청 처리 (취소)
    @DeleteMapping("/pre/{subjectId}")
    public ResponseEntity<?> deletePreStuSubAppProc(@PathVariable("subjectId") Long subjectId, @RequestParam Long type,
                                                    @AuthenticationPrincipal CustomUserDetails principal) {
        // type은 어디에서 넘어왔냐? 인 것 같음...

        // 예비 수강 신청 기간이 아니라면
        if (SUGANG_PERIOD != 0) {
            throw new CustomRestfullException("예비 수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        Long studentId = principal.getId();
        preStuSubService.deletePreStuSub(studentId, subjectId);

//		// 강의 검색 페이지에서 취소 시 -> 이제 프론트에서 navigate 처리할 수 있기 때문에 아래로 통일했습니다.
//		if (type == 0) { // 타입이 뭔지 알아보기...
//			return "redirect:/sugang/pre/1";
//			// 수강 신청 내역 페이지에서 취소 시
//		} else {
//			return "redirect:/sugang/preAppList?type=0";
//		}

        return ResponseEntity.ok().body("수강 신청이 정상적으로 처리되었습니다.");

    }

    // 예비 수강 신청 강의 목록에서 필터링
    @GetMapping("/pre/search")
    public ResponseEntity<?> preStuSubApplicationSearch(
            @Validated CurrentSemesterSubjectSearchFormDto currentSemesterSubjectSearchFormDto,
            @AuthenticationPrincipal CustomUserDetails principal) {

        // 예비 수강 신청 기간이 아니라면
        if (SUGANG_PERIOD != 0) {
            throw new CustomRestfullException("예비 수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        Long studentId = principal.getId();

        // 강의 리스트
        List<SubjectDto> subjectList = subjectService
                .readSubjectListSearchByCurrentSemester(currentSemesterSubjectSearchFormDto);

        for (SubjectDto sub : subjectList) {
            // 현재 담겨 있는지 확인
            PreStuSub preStuSub = preStuSubService.readPreStuSub(studentId, sub.getId());
            sub.setStatus(preStuSub != null);
        }

        Long subjectCount = (long) subjectList.size();

        // 필터에 사용할 전체 학과 정보
        List<Department> deptList = collegeService.readDeptAll();

        // 필터에 사용할 강의 이름 정보 (중복 값 제거)
        List<String> subNameList = new ArrayList<>();
        for (SubjectDto subject : subjectService.readSubjectListByCurrentSemester()) {
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

    /**
     * @return 수강 신청
     */
    @GetMapping("/application/{page}")
    public ResponseEntity<?> stuSubApplication(@PathVariable int page,
                                               @AuthenticationPrincipal CustomUserDetails principal) { // page int로 변경함

        // 수강 신청 기간이 아니라면
        if (SUGANG_PERIOD != 1) {
            throw new CustomRestfullException("수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
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
        ;
        // 현재 페이지 Todo 페이지 타입 처리하기
//        Page<SubjectDto> subjectListLimit = subjectService.readSubjectListByCurrentSemesterPage(subjectList, page);
//        for (SubjectDto sub : subjectListLimit) {
//            // 현재 담겨 있는지 확인
//            StuSub stuSub = stuSubService.readStuSub(principal.getId(), sub.getId());
//            if (stuSub != null) {
//                sub.setStatus(true);
//            } else {
//                sub.setStatus(false);
//            }
//        }

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
        if (SUGANG_PERIOD != 1) {
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

    /**
     * 수강 신청 처리 (신청)
     */
    @PostMapping("/insertApp/{subjectId}")
    public ResponseEntity<?> insertStuSubAppProc(@PathVariable("subjectId") Long subjectId, @RequestParam Long type,
                                                 @AuthenticationPrincipal CustomUserDetails principal) {

        // 수강 신청 기간이 아니라면
        if (SUGANG_PERIOD != 1) {
            throw new CustomRestfullException("수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        Long studentId = principal.getId();
        stuSubService.createStuSub(studentId, subjectId);

//		// 강의 검색 페이지에서 신청 시 -> 이제 프론트에서 navigate 처리할 수 있기 때문에 아래로 통일했습니다.
//		if (type == 0) {
//			return "redirect:/sugang/application/1";
//			// 예비 수강 신청 내역 페이지에서 신청 시
//		} else {
//			return "redirect:/sugang/preAppList?type=1";
//		}

        return ResponseEntity.ok().body("수강 신청이 정상적으로 처리되었습니다.");
    }

    /**
     * 수강 신청 처리 (취소)
     */
    @DeleteMapping("/deleteApp/{subjectId}")
    public ResponseEntity<?> deleteStuSubAppProc(@PathVariable("subjectId") Long subjectId, @RequestParam Long type,
                                                 @AuthenticationPrincipal CustomUserDetails principal) {

        // 수강 신청 기간이 아니라면
        if (SUGANG_PERIOD != 1) {
            throw new CustomRestfullException("수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        Long studentId = principal.getId();
        stuSubService.deleteStuSub(studentId, subjectId);

        return ResponseEntity.ok().body("수강 신청 취소가 정상적으로 처리되었습니다.");
    }

    /**
     * @return 예비 수강 신청 내역
     */
    @GetMapping("/preAppList")
    public ResponseEntity<?> preStuSubAppList(@RequestParam Long type,
                                              @AuthenticationPrincipal CustomUserDetails principal) {

        // 이번 학기에 재학 상태가 되지 않는 학생이라면 진입 불가
        Long studentId = principal.getId();
        StudentDto studentInfo = userService.readStudent(studentId);

        StuStatDto stuStatEntity = stuStatService.readCurrentStatus(studentInfo.getId());
        List<BreakApp> breakAppList = breakAppService.readByStudentId(studentInfo.getId()); // 최근 순으로 정렬되어 있음
        StuStatUtil.checkStuStat("수강신청", stuStatEntity, breakAppList);

        // 예비 수강 신청 기간에 조회 시
        if (type == 0) {
            List<StuSubAppDto> preStuSubList = preStuSubService.readPreStuSubList(studentId);

            Long sumGrades = 0L;
            for (StuSubAppDto s : preStuSubList) {
                sumGrades += s.getGrades();
            }

            return ResponseEntity.ok(Map.of(
                    "type", type,
                    "stuSubList", preStuSubList,
                    "sumGrades", sumGrades
            ));
        }

        // 수강 신청 기간에 조회 시
        // (예비 수강 신청 목록에서 수강 신청으로 자동으로 넘어간 강의와, 직접 신청해야 하는 강의를 분리해서 보여줄것)

        // 수강 신청 기간이 아니라면
        if (SUGANG_PERIOD != 1) {
            throw new CustomRestfullException("수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
        }

        // 수강 신청이 완료되지 않은 예비 수강 신청 내역
        List<StuSubAppDto> preStuSubList1 = stuSubService.readPreStuSubByStuSub(studentId);

        // 수강 신청 내역
        List<StuSubAppDto> stuSubList = stuSubService.readStuSubList(studentId);

        Long sumGrades = 0L;
        for (StuSubAppDto s : stuSubList) {
            sumGrades += s.getGrades();
        }

        return ResponseEntity.ok(Map.of(
                "type", type,
                "stuSubList", preStuSubList1,
                "sumGrades", sumGrades
        ));
    }

    /**
     * @return 수강 신청 내역
     */
    @GetMapping("/list")
    public ResponseEntity<?> stuSubAppList(@AuthenticationPrincipal CustomUserDetails principal) {

        // 예비 수강 신청 기간이라면
        if (SUGANG_PERIOD == 0) {
            throw new CustomRestfullException("수강 신청 기간이 아닙니다.", HttpStatus.BAD_REQUEST);
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

        Long sumGrades = 0L;
        for (StuSubAppDto s : stuSubList) {
            sumGrades += s.getGrades();
        }

        return ResponseEntity.ok(Map.of(
                "stuSubList", stuSubList,
                "sumGrades", sumGrades
        ));
    }

}
