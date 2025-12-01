package com.green.university.controller;

import com.green.university.dto.*;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.repository.model.Professor;
import com.green.university.repository.model.Student;
import com.green.university.service.ProfessorService;
import com.green.university.service.StudentService;
import com.green.university.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 유저 페이지
 * 
 * @author 김지현
 */
@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private StudentService studentService;
	@Autowired
	private ProfessorService professorService;

	/**
	 * @return staff 입력 페이지
	 */
	@GetMapping("/staff")
	public String createStaff() {

		return "/user/createStaff";
	}

	/**
	 * staff 입력 post 처리
	 * 
	 * @param createStaffDto
	 * @return "redirect:/user/staff"
	 */
	@PostMapping("/staff")
	public String createStaffProc(@Valid CreateStaffDto createStaffDto, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			bindingResult.getAllErrors().forEach(error -> {
				sb.append(error.getDefaultMessage()).append("\\n");
			});
			throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
		}
		userService.createStaffToStaffAndUser(createStaffDto);

		return "redirect:/user/staff";
	}

	/**
	 * @return professor 입력 페이지
	 */
	@GetMapping("/professor")
	public String createProfessor() {

		return "/user/createProfessor";
	}

	/**
	 * staff 입력 post 처리
	 * 
	 * @param createProfessorDto
	 * @return "redirect:/user/professor"
	 */
	@PostMapping("/professor")
	public String createProfessorProc(@Valid CreateProfessorDto createProfessorDto, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			bindingResult.getAllErrors().forEach(error -> {
				sb.append(error.getDefaultMessage()).append("\\n");
			});
			throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
		}

		userService.createProfessorToProfessorAndUser(createProfessorDto);

		return "redirect:/user/professor";
	}

	/**
	 * @return student 입력 페이지
	 */
	@GetMapping("/student")
	public String createStudent() {

		return "/user/createStudent";
	}

	/**
	 * student 입력 post 처리
	 * 
	 * @param createStudentDto
	 * @return "redirect:/user/student"
	 */
	@PostMapping("/student")
	public String createStudentProc(@Valid CreateStudentDto createStudentDto, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			bindingResult.getAllErrors().forEach(error -> {
				sb.append(error.getDefaultMessage()).append("\\n");
			});
			throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
		}

		userService.createStudentToStudentAndUser(createStudentDto);

		return "redirect:/user/student";
	}

	/**
	 * 교수 조회
	 * 
	 * @param model
	 * @return 교수 조회 페이지
	 */
	@GetMapping("/professorList")
	public String showProfessorList(Model model, @RequestParam(required = false) Long professorId,
			@RequestParam(required = false) Long deptId) {

		ProfessorListForm professorListForm = new ProfessorListForm();
		professorListForm.setPage(0L);
		if (professorId != null) {
			professorListForm.setProfessorId(professorId);
		} else if (deptId != null) {
			professorListForm.setDeptId(deptId);
		}
		Long amount = professorService.readProfessorAmount(professorListForm);
		if (professorId != null) {
			amount = 1L;
		}
		List<Professor> list = professorService.readProfessorList(professorListForm);

		model.addAttribute("listCount", Math.ceil(amount / 20.0));
		model.addAttribute("professorList", list);
		model.addAttribute("deptId", deptId);
		/**
		 * @author 서영 1페이지가 선택되어 있음을 보여주기 위함
		 */
		model.addAttribute("page", 1);

		return "/user/professorList";
	}

	/**
	 * 교수 조회
	 * 
	 * @param model
	 * @return 교수 조회 페이지
	 */
	@GetMapping("/professorList/{page}")
	public String showProfessorListByPage(Model model, @PathVariable Long page,
			@RequestParam(required = false) Long deptId) {

		ProfessorListForm professorListForm = new ProfessorListForm();
		if (deptId != null) {
			professorListForm.setDeptId(deptId);
		}
		professorListForm.setPage((page - 1) * 20);
		Long amount = professorService.readProfessorAmount(professorListForm);
		List<Professor> list = professorService.readProfessorList(professorListForm);

		model.addAttribute("listCount", Math.ceil(amount / 20.0));
		model.addAttribute("professorList", list);
		model.addAttribute("page", page);

		return "/user/professorList";
	}

	/**
	 * 학생 조회
	 * 
	 * @param model
	 * @return 학생 조회 페이지
	 */
	@GetMapping("/studentList")
	public String showStudentList(Model model, @RequestParam(required = false) Long studentId,
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

		model.addAttribute("listCount", Math.ceil(amount / 20.0));
		model.addAttribute("studentList", list);
		model.addAttribute("deptId", deptId);
		/**
		 * @author 서영 1페이지가 선택되어 있음을 보여주기 위함
		 */
		model.addAttribute("page", 1);

		return "/user/studentList";
	}

	/**
	 * 학생 조회
	 * 
	 * @param model
	 * @return 학생 조회 페이지
	 */
	@GetMapping("/studentList/{page}")
	public String showStudentListByPage(Model model, @PathVariable Long page,
			@RequestParam(required = false) Long deptId) {

		StudentListForm studentListForm = new StudentListForm();
		if (deptId != null) {
			studentListForm.setDeptId(deptId);
		}
		studentListForm.setPage((page - 1) * 20);
		Long amount = studentService.readStudentAmount(studentListForm);
		List<Student> list = studentService.readStudentList(studentListForm);

		model.addAttribute("listCount", Math.ceil(amount / 20.0));
		model.addAttribute("studentList", list);
		model.addAttribute("page", page);

		return "/user/studentList";
	}

	/**
	 * 학생의 학년, 학기 업데이트
	 * 
	 * @return 학생 리스트 조회 페이지
	 */
	@GetMapping("/student/update")
	public String updateStudentGradeAndSemester() {
		studentService.updateStudentGradeAndSemester();
		return "redirect:/user/studentList";
	}

}
