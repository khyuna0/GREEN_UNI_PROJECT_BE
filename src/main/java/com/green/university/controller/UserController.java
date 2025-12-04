package com.green.university.controller;

import com.green.university.dto.*;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.entity.Professor;
import com.green.university.entity.Student;
import com.green.university.service.ProfessorService;
import com.green.university.service.StudentService;
import com.green.university.service.UserService;
import com.green.university.utils.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 유저 페이지
 * 
 * @author 김지현
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private StudentService studentService;
	@Autowired
	private ProfessorService professorService;

//	/**
//	 * @return staff 입력 페이지
//	 */
//	@GetMapping("/staff")
//	public ResponseEntity<?> createStaff() {
//
//        return ResponseEntity.ok().body("staff 입력 페이지");
//	}

	/**
	 * staff 입력 post 처리
	 * 
	 * @param createStaffDto
	 * @return "redirect:/user/staff"
	 */
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

//	/**
//	 * @return professor 입력 페이지
//	 */
//	@GetMapping("/professor")
//	public ResponseEntity<?> createProfessor() {
//
//        return ResponseEntity.ok().body("교수 입력이 완료되었습니다.");
//	}

	/**
	 * staff 입력 post 처리
	 * 
	 * @param createProfessorDto
	 * @return "redirect:/user/professor"
	 */
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

//	/**
//	 * @return student 입력 페이지
//	 */
//	@GetMapping("/student")
//	public ResponseEntity<?> createStudent() {
//
//        return ResponseEntity.ok().body("학생 입력이 완료되었습니다.");
//	}

	/**
	 * student 입력 post 처리
	 * 
	 * @param createStudentDto
	 * @return "redirect:/user/student"
	 */
	@PostMapping("/student")
	public ResponseEntity<?> createStudentProc(@Valid CreateStudentDto createStudentDto, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			bindingResult.getAllErrors().forEach(error -> {
				sb.append(error.getDefaultMessage()).append("\\n");
			});
			throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
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

        PaginationUtil.PaginationResult paginationResult = PaginationUtil.build(list, currentPage, 10 )

        return ResponseEntity.ok(Map.of(
                "professorList", list,
                "paginationResult", paginationResult
                // "deptId", deptId,
        ));
	}

//	/**
//	 * 교수 조회
//	 *
//	 * @return 교수 조회 페이지
//	 */
//	@GetMapping("/professorList/{page}")
//	public ResponseEntity<?> showProfessorListByPage( @PathVariable int page,
//			@RequestParam(required = false) Long deptId) {
//
//		ProfessorListForm professorListForm = new ProfessorListForm();
//		if (deptId != null) {
//			professorListForm.setDeptId(deptId);
//		}
//		professorListForm.setPage((page - 1) * 20);
//		Long amount = professorService.readProfessorAmount(professorListForm);
//		List<Professor> list = professorService.readProfessorList(professorListForm);
//
//        return ResponseEntity.ok(Map.of(
//                "listCount", Math.ceil(amount / 20.0),
//                "professorList", list,
//                "page", page
//        ));
//	}




	/**
	 * 학생 조회
	 *
	 * @return 학생 조회 페이지
	 */
	@GetMapping("/studentList")
	public ResponseEntity<?> showStudentList( @RequestParam(required = false) Long studentId,
			@RequestParam(required = false) Long deptId) {

		StudentListForm studentListForm = new StudentListForm();
		studentListForm.setPage(0L);
		if (studentId != null) {
			studentListForm.setStudentId(studentId);
		} else if (deptId != null) {
			studentListForm.setDeptId(deptId);
		}
		Long amount = studentService.readStudentAmount(studentListForm);
		if (studentId != null) {
			amount = 1L;
		}
		List<Student> list = studentService.readStudentList(studentListForm);

		/**
		 * @author 서영 1페이지가 선택되어 있음을 보여주기 위함
		 */

        return ResponseEntity.ok(Map.of(
                "listCount", Math.ceil(amount / 20.0),
                "studentList", list,
                "deptId", deptId,
                "page", 1
        ));
	}

	/**
	 * 학생 조회
	 *
	 * @return 학생 조회 페이지
	 */
	@GetMapping("/studentList/{page}")
	public ResponseEntity<?> showStudentListByPage( @PathVariable Long page,
			@RequestParam(required = false) Long deptId) {

		StudentListForm studentListForm = new StudentListForm();
		if (deptId != null) {
			studentListForm.setDeptId(deptId);
		}
		studentListForm.setPage((page - 1) * 20);
		Long amount = studentService.readStudentAmount(studentListForm);
		List<Student> list = studentService.readStudentList(studentListForm);

        return ResponseEntity.ok(Map.of(
                "listCount", Math.ceil(amount / 20.0),
                "studentList", list,
                "deptId", deptId,
                "page", 1
        ));
	}

	/**
	 * 학생의 학년, 학기 업데이트
	 * 
	 * @return 학생 리스트 조회 페이지
	 */
	@GetMapping("/student/update")
	public ResponseEntity<?> updateStudentGradeAndSemester() {
		studentService.updateStudentGradeAndSemester();
        return ResponseEntity.ok().body("학생 조회가 완료되었습니다.");
	}

}
