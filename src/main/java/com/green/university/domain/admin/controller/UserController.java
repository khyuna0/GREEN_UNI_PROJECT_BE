package com.green.university.domain.admin.controller;

import com.green.university.domain.admin.dto.CreateStaffFormDto;
import com.green.university.domain.professor.dto.CreateProfessorFormDto;
import com.green.university.domain.student.dto.CreateStudentFormDto;
import com.green.university.domain.professor.dto.ProfessorDto;
import com.green.university.domain.student.dto.StudentDto;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.domain.professor.service.ProfessorService;
import com.green.university.domain.student.service.StudentService;
import com.green.university.domain.admin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;


// 유저 페이지
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private ProfessorService professorService;

    private static final int PAGE_SIZE = 20;

    // staff 입력
    @PostMapping("/staff")
    public ResponseEntity<?> createStaffProc(@Valid @RequestBody CreateStaffFormDto createStaffFormDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(error -> {
                sb.append(error.getDefaultMessage()).append("\\n");
            });
            throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
        }
        userService.createStaffToStaffAndUser(createStaffFormDto);

        return ResponseEntity.ok().body("직원 입력이 완료되었습니다.");
    }


    // professor 입력
    @PostMapping("/professor")
    public ResponseEntity<?> createProfessorProc(@Valid @RequestBody CreateProfessorFormDto createProfessorFormDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(error -> {
                sb.append(error.getDefaultMessage()).append("\\n");
            });
            throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        userService.createProfessorToProfessorAndUser(createProfessorFormDto);

        return ResponseEntity.ok().body("교수 입력이 완료되었습니다.");
    }


    // student 입력
    @PostMapping("/student")
    public ResponseEntity<?> createStudentProc(@Valid @RequestBody CreateStudentFormDto createStudentFormDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(err -> {
                        errors.put(err.getField(), err.getDefaultMessage());
                    }
            );
            System.out.println("학생오류" + errors);
        }

        userService.createStudentToStudentAndUser(createStudentFormDto);
        return ResponseEntity.ok().body("학생 입력이 완료되었습니다.");
    }

    /**
     * 교수 조회+검색+페이징
     *
     * @return 교수 조회 페이지 (아래 showProfessorListByPage와 합칠 수 있을 것 같다)
     */
    @GetMapping({"/professorList/{page}"})
    public ResponseEntity<?> showProfessorList(
            @PathVariable int page,   // 0부터 시작함
            @RequestParam(required = false) Long professorId,
            @RequestParam(required = false) String deptName
    ) {
        if(page < 0) page = 0; // 페이지 유효성 검사

        Pageable pageable = PageRequest.of(page ,PAGE_SIZE, Sort.by("id").descending());

        Page<ProfessorDto> list = professorService.readProfessorList(professorId, deptName, pageable);

        return ResponseEntity.ok(Map.of(
                "professorList", list.getContent(),    // 실제 데이터
                "page", list.getNumber(),               //  현재 페이지
                "totalPages", list.getTotalPages(),     // 전체 페이지 수
                "totalElements", list.getTotalElements()// 전체 개수
        ));
    }

    // 학생 조회 페이지 ("/user/studentList")
    @GetMapping("/studentList/{page}")
    public ResponseEntity<?> showStudentList(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long deptId,
            @PathVariable int page) {

        if(page < 0) page = 0; // 페이지 유효성 검사

        Pageable pageable = PageRequest.of(page ,PAGE_SIZE, Sort.by("id").descending());

        Page<StudentDto> list = studentService.readStudentList(studentId, deptId, pageable);
        System.out.println(list);

        Map<String, Object> pagingResponse = new HashMap<>();
        pagingResponse.put("listCount", list.getTotalElements()); // 전체 글 수
        pagingResponse.put("totalPages", list.getTotalPages()); // 전체 페이지 수
        pagingResponse.put("currentPage", list.getNumber()); // 현재 페이지 번호
        pagingResponse.put("studentList", list.getContent()); // 현재 페이지에 해당하는 데이터

        return ResponseEntity.ok(pagingResponse);
    }

    // 학생의 학년, 학기 업데이트
    @PostMapping("/student/update-grade-semester")
    public ResponseEntity<?> updateStudentGradeAndSemester() {
        int updatedCount = studentService.updateStudentGradeAndSemester();
        return ResponseEntity.ok(updatedCount + "명 학년, 학기 업데이트 완료");
    }

}
