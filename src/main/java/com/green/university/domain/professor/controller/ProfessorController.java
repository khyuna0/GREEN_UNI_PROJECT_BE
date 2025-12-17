package com.green.university.domain.professor.controller;

import com.green.university.domain.professor.dto.ReadSyllabusDto;
import com.green.university.domain.professor.dto.SyllaBusFormDto;
import com.green.university.domain.professor.service.ProfessorService;
import com.green.university.domain.student.dto.StudentInfoForProfessorDto;
import com.green.university.domain.subject.dto.SubjectForProfessorDto;
import com.green.university.domain.subject.dto.SubjectPeriodForProfessorDto;
import com.green.university.domain.subject.dto.UpdateStudentGradeFormDto;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.domain.subject.repository.SubjectAiJobRepository;
import com.green.university.domain.subject.service.StuSubDetailService;
import com.green.university.global.security.CustomUserDetails;
import com.green.university.global.utils.Define;
import com.green.university.global.utils.TermUtil;
import com.green.university.infra.ai.dto.response.SubjectAiStatusResponse;
import com.green.university.infra.ai.entity.SubjectAiJob;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// 교수 행정 페이지 (자기과목 조회, 학생 성적 기입)
@RestController
@RequestMapping("/api/professor")
public class ProfessorController {

	@Autowired
	private ProfessorService professorService;
    @Autowired
    private StuSubDetailService stuSubDetailService;
	@Autowired
	private SubjectAiJobRepository subjectAiJobRepository;


	// 교수가 성적을 최종으로 확정 지으면 ai가 돌아감
	@PostMapping("/subjects/{subjectId}/finalize")
	public ResponseEntity<Void> finalizeSubjectGrades(@PathVariable Long subjectId) {
		professorService.finalizeGrades(subjectId);
		return ResponseEntity.ok().build(); // 바로 200 리턴 (AI는 백그라운드)
	}

	@GetMapping("/subjects/{subjectId}/ai-status")
	public ResponseEntity<SubjectAiStatusResponse> aiStatus(@PathVariable Long subjectId) {
		SubjectAiJob job = subjectAiJobRepository.findBySubject_Id(subjectId)
				.orElse(null);

		if (job == null) {
			return ResponseEntity.ok(new SubjectAiStatusResponse("IDLE", "아직 AI 분석을 시작하지 않았습니다.", 0, 0));
		}

		return ResponseEntity.ok(new SubjectAiStatusResponse(
				job.getStatus().name(),
				job.getMessage(),
				job.getDoneCount(),
				job.getTotalCount()
		));
	}






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
		subjectPeriodForProfessorDto.setSubYear(TermUtil.currentYear());
		subjectPeriodForProfessorDto.setSemester(TermUtil.currentSemester());
		subjectPeriodForProfessorDto.setId(professorId);
		List<SubjectForProfessorDto> subjectList = professorService.selectSubjectBySemester(subjectPeriodForProfessorDto);

        return ResponseEntity.ok(Map.of(
                "semesterList", semesterList,
                "subjectList", subjectList
        ));
	}

	/**
	 * 조회한 년도 학기의 강의 리스트 출력
     * subject를 semester와 year로 찾기
	 *
	 * @param period: 조회할 년도 학기
	 * @return 조회 신청한 학기의 본인 강좌 조회 페이지
	 */
	@PostMapping("/subject")
	public ResponseEntity<?> subjectListProc( @RequestParam String period,
                                              @AuthenticationPrincipal CustomUserDetails principal) { // period는 "2023년도 1학기" 형식
		Long professorId = principal.getId();
		List<SubjectPeriodForProfessorDto> semesterList = professorService.selectSemester(professorId);
		String[] str = period.split("_");
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
	 * @return 해당 과목을 듣는 학생 리스트
	 */
	@GetMapping("/subject/{subjectId}")
	public ResponseEntity<?> subjectStudentList(@PathVariable("subjectId") Long subjectId) {
		List<StudentInfoForProfessorDto> studentList = professorService.selectBySubjectId(subjectId);
		Subject subject = professorService.selectSubjectById(subjectId);
        int stuNum = studentList.size();
        return ResponseEntity.ok(Map.of(
                "subject", subject,
                "studentList", studentList,
                "stuNum", stuNum
        ));
	}

    // 교수의 성적 입력 (절대평가 : 등급까지 산출 / 상대평가 : 환산점수까지만 산출, 과락, 결석 F 처리만)
    @PatchMapping("/subject/{subjectId}/{studentId}")
    public ResponseEntity<?> updateStudentDetailProc(
            @PathVariable Long subjectId,
            @PathVariable Long studentId,
            @Valid @RequestBody UpdateStudentGradeFormDto dto) {

        // 점수 입력 시 결석 횟수 따라 F, 환산점수 처리
        professorService.updateGrade(subjectId, studentId, dto);

        return ResponseEntity.ok(Map.of(
                "studentId", studentId,
                "subjectId", subjectId
        ));
    }

    // 상대평가 과목: 전체 학생 등급 산출
    @PatchMapping("/relativeGrade/{subjectId}")
    public ResponseEntity<?> relativeGrade (
            @PathVariable Long subjectId
    ) {
        stuSubDetailService.getRelativeGrade(subjectId);

        return ResponseEntity.ok().body("등급 산출 완료");
    }

	/**
	 *
	 * @return 강의계획서 조회 창
	 */
	@GetMapping("/syllabus/{subjectId}")
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
	@PatchMapping("/syllabus/{subjectId}")
	public ResponseEntity<?> createSyllabusProc(@PathVariable("subjectId") Long subjectId, @RequestBody SyllaBusFormDto syllaBusFormDto) {
		professorService.updateSyllabus(subjectId, syllaBusFormDto);

        return ResponseEntity.ok().body("강의 계획서 수정이 정상적으로 처리되었습니다.");
	}

}
