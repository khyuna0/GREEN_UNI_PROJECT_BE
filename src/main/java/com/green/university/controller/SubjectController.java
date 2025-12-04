package com.green.university.controller;

import com.green.university.dto.AllSubjectSearchFormDto;
import com.green.university.dto.response.ReadSyllabusDto;
import com.green.university.dto.response.SubjectDto;
import com.green.university.entity.Department;
import com.green.university.service.CollegeService;
import com.green.university.service.ProfessorService;
import com.green.university.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 서영 
 * 강의 목록
 */

@RestController
@RequestMapping("/subject")
public class SubjectController {

	@Autowired
	private SubjectService subjectService;

	@Autowired
	private CollegeService collegeService;

	@Autowired
	private ProfessorService professorService;

	// 모든 강의 조회 (모든 연도-학기에 대해서)
	@GetMapping("/list/{page}")
	public ResponseEntity<?> readSubjectList(@PathVariable int page) {
		// 강의 리스트 (전체)
		List<SubjectDto> subjectList = subjectService.readSubjectList();

		int subjectCount = subjectList.size(); // 기존 long에서 int로 변경
		// 총 페이지 수
		int pageCount = (int) Math.ceil(subjectCount / 20.0);
		// 현재 페이지
		Page<SubjectDto> subjectListLimit = subjectService.readSubjectListPage((page - 1) * 20);

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
                "subjectList", subjectListLimit,
                "deptList", deptList
        ));
	}

	// 전체 강의 목록에서 필터링
	@GetMapping("/list/search")
	public ResponseEntity<?> readSubjectListSearch(@Validated AllSubjectSearchFormDto allSubjectSearchFormDto) {

		// 강의 리스트
		List<SubjectDto> subjectList = subjectService.readSubjectListSearch(allSubjectSearchFormDto);
		int subjectCount = subjectList.size(); // int로 변경

		// 필터에 사용할 전체 학과 정보
		List<Department> deptList = collegeService.readDeptAll();

		// 필터에 사용할 강의 이름 정보 (중복 값 제거)
		List<String> subNameList = new ArrayList<>();
		for (SubjectDto subject : subjectService.readSubjectList()) {
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
	 * @author 김지현
	 * @param subjectId
	 * @return 강의계획서 조회
	 */
	@GetMapping("/syllabus/{subjectId}")
	public ResponseEntity<?> readSyllabus(@PathVariable("subjectId") Long subjectId) {
		ReadSyllabusDto readSyllabusDto = professorService.readSyllabus(subjectId);
		if (readSyllabusDto.getOverview() != null) {
			readSyllabusDto.setOverview(readSyllabusDto.getOverview().replace("\r\n", "<br>"));
		}
		if (readSyllabusDto.getObjective() != null) {
			readSyllabusDto.setObjective(readSyllabusDto.getObjective().replace("\r\n", "<br>"));
		}
		if (readSyllabusDto.getProgram() != null) {
			readSyllabusDto.setProgram(readSyllabusDto.getProgram().replace("\r\n", "<br>"));
		}

        return ResponseEntity.ok(Map.of(
                "syllabus", readSyllabusDto
        ));
	}

}
