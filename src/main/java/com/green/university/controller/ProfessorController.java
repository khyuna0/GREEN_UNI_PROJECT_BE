package com.green.university.controller;

import com.green.university.dto.SyllaBusFormDto;
import com.green.university.dto.UpdateStudentGradeDto;
import com.green.university.dto.response.*;
import com.green.university.entity.Student;
import com.green.university.entity.Subject;
import com.green.university.security.CustomUserDetails;
import com.green.university.service.ProfessorService;
import com.green.university.service.StuSubService;
import com.green.university.service.SubjectService;
import com.green.university.service.UserService;
import com.green.university.utils.Define;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * 교수 행정 페이지 (자기과목 조회, 학생 성적 기입)
 * 
 * @author 김지현
 */
@RestController
@RequestMapping("/api/professor")
public class ProfessorController {

	@Autowired
	private ProfessorService professorService;
	@Autowired
	private HttpSession session;
	@Autowired
	private UserService userService;
	@Autowired
	private StuSubService stuSubService;
	@Autowired
	private SubjectService subjectService;
	
	/**
	 * 교수 본인의 강의가 있는 년도 학기 조회하는 기능 조회한 년도 학기의 강의 리스트 출력(처음값은 현재학기)
	 *
	 * @return 본인 강좌 조회 페이지
	 */
	@GetMapping("/subject")
	public ResponseEntity<?> subjectList(@AuthenticationPrincipal CustomUserDetails principal) {

        Long professorId = principal.getId();

		List<SubjectPeriodForProfessorDto> semesterList = professorService.selectSemester(professorId);
		SubjectPeriodForProfessorDto subjectPeriodForProfessorDto = new SubjectPeriodForProfessorDto();
		subjectPeriodForProfessorDto.setSubYear(Define.CURRENT_YEAR);
		subjectPeriodForProfessorDto.setSemester(Define.CURRENT_SEMESTER);
		subjectPeriodForProfessorDto.setId(professorId);
		List<SubjectForProfessorDto> subjectList = professorService
				.selectSubjectBySemester(subjectPeriodForProfessorDto);

        return ResponseEntity.ok(Map.of(
                "semesterList", semesterList,
                "subjectList", subjectList
        ));
	}

	/**
	 * 조회한 년도 학기의 강의 리스트 출력
	 *
	 * @param period: 조회할 년도 학기
	 * @return 조회 신청한 학기의 본인 강좌 조회 페이지
	 */
	@PostMapping("/subject")
	public ResponseEntity<?> subjectListProc( @RequestParam String period,
                                              @AuthenticationPrincipal CustomUserDetails principal) { // period는 "2023년도 1학기" 형식
		Long professorId = principal.getId();
		List<SubjectPeriodForProfessorDto> semesterList = professorService.selectSemester(professorId);
		String[] str = period.split("year");
		SubjectPeriodForProfessorDto subjectPeriodForProfessorDto = new SubjectPeriodForProfessorDto();
		subjectPeriodForProfessorDto.setSubYear(Long.valueOf(str[0]));
		subjectPeriodForProfessorDto.setSemester(Long.valueOf(str[1]));
		subjectPeriodForProfessorDto.setId(professorId);
		List<SubjectForProfessorDto> subjectList = professorService.selectSubjectBySemester(subjectPeriodForProfessorDto);

        return ResponseEntity.ok(Map.of(
                "semesterList", semesterList,
                "subjectList", subjectList
        ));
	}

	/**
	 *
	 * @return 해당 과목을 듣는 학생 리스트
	 */
	@GetMapping("/subject/{subjectId}")
	public ResponseEntity<?> subjectStudentList( @PathVariable("subjectId") Long subjectId) {
		List<StudentInfoForProfessorDto> studentList = professorService.selectBySubjectId(subjectId);
		Subject subject = professorService.selectSubjectById(subjectId);

        return ResponseEntity.ok(Map.of(
                "subject", subject,
                "studentList", studentList
        ));
	}

	/**
	 *
	 * @param subjectId
	 * @param studentId
	 * @return 출결 및 성적 기입 페이지
	 */
	@GetMapping("/subject/{subjectId}/{studentId}")
	public ResponseEntity<?> updateStudentDetail(@PathVariable("subjectId") Long subjectId, @PathVariable ("studentId")Long studentId) {

        StudentDto student = userService.readStudent(studentId);

        return ResponseEntity.ok(Map.of(
                "student", student,
                "subjectId", subjectId
        ));
	}
    
    // 업데이트
	@PatchMapping("/subject/{subjectId}/{studentId}")
	public ResponseEntity<?> updateStudentDetailProc(@PathVariable("subjectId") Long subjectId, @PathVariable ("studentId")Long studentId,
			UpdateStudentGradeDto updateStudentGradeDto) {

		// 점수 입력
		professorService.updateGrade(updateStudentGradeDto);
		
		// 취득학점 입력
		if (updateStudentGradeDto.getGrade().equals("F")) {
			stuSubService.updateCompleteGrade(studentId, subjectId, 0L);
		} else {
			Long subjectGrade = subjectService.readBySubjectId(subjectId).getGrades();
			stuSubService.updateCompleteGrade(studentId, subjectId, subjectGrade);
		}

        return ResponseEntity.ok(Map.of(
                "studentId", studentId,
                "subjectId", subjectId
        ));
	}

	/**
	 *
	 * @return 강의계획서 조회 창
	 */
	@GetMapping("/syllabus/update/{subjectId}")
	public ResponseEntity<?> createSyllabus(@PathVariable("subjectId") Long subjectId) {
		ReadSyllabusDto readSyllabusDto = professorService.readSyllabus(subjectId);

        return ResponseEntity.ok(Map.of(
                "syllabus", readSyllabusDto
        ));
	}

	/**
	 * 
	 * @param syllaBusFormDto
	 * @return 강의계획서 업데이트 창
	 */
	@PatchMapping("/syllabus/update/{subjectId}")
	public ResponseEntity<?> createSyllabusProc(@PathVariable("subjectId") Long subjectId, SyllaBusFormDto syllaBusFormDto) {
//		System.out.println(syllaBusFormDto);
		professorService.updateSyllabus(subjectId, syllaBusFormDto);

        return ResponseEntity.ok().body("강의 계획서 수정이 정상적으로 처리되었습니다.");
	}

}
