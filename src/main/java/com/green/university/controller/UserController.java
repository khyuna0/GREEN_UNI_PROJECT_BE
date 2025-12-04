package com.green.university.controller;

import com.green.university.dto.*;
import com.green.university.dto.response.StudentDto;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.entity.Professor;
import com.green.university.service.ProfessorService;
import com.green.university.service.StudentService;
import com.green.university.service.UserService;
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

    /**
     * @return staff 입력 페이지
     */
    @GetMapping("/staff")
    public ResponseEntity<?> createStaff() {

        return "/user/createStaff";
    }

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

        return "redirect:/user/staff";
    }

    /**
     * @return professor 입력 페이지
     */
    @GetMapping("/professor")
    public ResponseEntity<?> createProfessor() {

        return "/user/createProfessor";
    }

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

        return "redirect:/user/professor";
    }

    /**
     * @return student 입력 페이지
     */
    @GetMapping("/student")
    public ResponseEntity<?> createStudent() {

        return "/user/createStudent";
    }

    /**
     * student 입력 post 처리
     *
     * @param createStudentDto
     * @return "redirect:/user/student"
     */
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
        return ResponseEntity.ok("학생이 등록되었습니다.");
    }

    /**
     * 교수 조회 (검색인 듯?)
     *
     * @return 교수 조회 페이지 (아래 showProfessorListByPage와 합칠 수 있을 것 같다)
     */
    @GetMapping("/professorList")
    public ResponseEntity<?> showProfessorList(@RequestParam(required = false) Long professorId,
                                               @RequestParam(required = false) Long deptId) {

        ProfessorListForm professorListForm = new ProfessorListForm();
        professorListForm.setPage(0);
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
     * @return 교수 조회 페이지
     */
    @GetMapping("/professorList/{page}")
    public ResponseEntity<?> showProfessorListByPage(@PathVariable int page,
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

    /**
     * 학생의 학년, 학기 업데이트
     *
     * @return 학생 리스트 조회 페이지
     */
    @PostMapping("/student/update-grade-semeste")
    public ResponseEntity<?> updateStudentGradeAndSemester() {
        int updatedCount = studentService.updateStudentGradeAndSemester();
        return ResponseEntity.ok(updatedCount + "명 학년, 학기 업데이트 완료");
    }

}
