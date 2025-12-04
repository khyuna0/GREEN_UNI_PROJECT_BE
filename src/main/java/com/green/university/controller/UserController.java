package com.green.university.controller;

import com.green.university.dto.*;
import com.green.university.dto.response.StudentDto;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.entity.Professor;
import com.green.university.service.ProfessorService;
import com.green.university.service.StudentService;
import com.green.university.service.UserService;
import com.green.university.utils.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


// 유저 페이지
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private ProfessorService professorService;


    // staff 입력
    @PostMapping("/staff")
    public ResponseEntity<?> createStaffProc(@Valid CreateStaffDto createStaffDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(error -> {
                sb.append(error.getDefaultMessage()).append("\\n");
            });
            throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
        }
        userService.createStaffToStaffAndUser(createStaffDto);

        return ResponseEntity.ok().body("직원 입력이 완료되었습니다.");
	}


	// professor 입력
	@PostMapping("/professor")
	public ResponseEntity<?> createProfessorProc(@Valid CreateProfessorDto createProfessorDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(error -> {
                sb.append(error.getDefaultMessage()).append("\\n");
            });
            throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        userService.createProfessorToProfessorAndUser(createProfessorDto);

        return ResponseEntity.ok().body("교수 입력이 완료되었습니다.");
	}



    // student 입력
    @PostMapping("/student")
    public ResponseEntity<?> createStudentProc(@Valid CreateStudentDto createStudentDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, Object> errors = new HashMap<>();
            bindingResult.getAllErrors().forEach(error -> {
                errors.put(error.getDefaultMessage(), error.getDefaultMessage());
            });
            throw new CustomRestfullException(errors.toString(), HttpStatus.BAD_REQUEST);
        }
        userService.createStudentToStudentAndUser(createStudentDto);
        return ResponseEntity.ok().body("학생 입력이 완료되었습니다.");
    }

	/**
	 * 교수 조회+검색+페이징
	 *
	 * @return 교수 조회 페이지 (아래 showProfessorListByPage와 합칠 수 있을 것 같다)
	 */
	@GetMapping({"/professorList", "/professorList/{page}"})
	public ResponseEntity<?> showProfessorList(
            @PathVariable(value = "page", required = false) Integer page,
            @RequestParam(required = false) Long professorId,
			@RequestParam(required = false) Long deptId) {

        int currentPage = ( page == null  || page < 1) ? 1 : page;

		ProfessorListForm professorListForm = new ProfessorListForm();
		professorListForm.setPage((currentPage - 1) * 20);

        // 검색어가 있는 경우
		if (professorId != null) professorListForm.setProfessorId(professorId);
        if (deptId != null) professorListForm.setDeptId(deptId);

        Page<Professor> list = professorService.readProfessorList(professorListForm, currentPage);
		/**
		 * @author 서영 1페이지가 선택되어 있음을 보여주기 위함
		 */

        PaginationUtil.PaginationResult paginationResult = PaginationUtil.build(list, currentPage, 10 );

        return ResponseEntity.ok(Map.of(
                "professorList", list,
                "paginationResult", paginationResult
                // "deptId", deptId,
        ));
	}

    // 학생 조회 페이지 ("/user/studentList")
    @GetMapping("/studentList")
    public ResponseEntity<?> showStudentList(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(defaultValue = "0") Integer page) {

        StudentListForm studentListForm = new StudentListForm();
        studentListForm.setStudentId(studentId);
        studentListForm.setDeptId(deptId);
        studentListForm.setPage(page.longValue());

        Page<StudentDto> list = studentService.readStudentList(studentListForm);

        Map<String, Object> pagingResponse = new HashMap<>();
        pagingResponse.put("listCount", list.getTotalElements()); // 전체 글 수
        pagingResponse.put("totalPages", list.getTotalPages()); // 전체 페이지 수
        pagingResponse.put("currentPage", list.getNumber()); // 현재 페이지 번호
        pagingResponse.put("lists", list.getContent()); // 현재 페이지에 해당하는 데이터

        return ResponseEntity.ok(pagingResponse);
    }

    // 학생의 학년, 학기 업데이트
    @PostMapping("/student/update-grade-semester")
    public ResponseEntity<?> updateStudentGradeAndSemester() {
        int updatedCount = studentService.updateStudentGradeAndSemester();
        return ResponseEntity.ok(updatedCount + "명 학년, 학기 업데이트 완료");
    }

}
