package com.green.university.controller;

import com.green.university.config.security.CustomUserDetails;
import com.green.university.dto.AllSubjectSearchFormDto;
import com.green.university.dto.response.ReadSyllabusDto;
import com.green.university.dto.response.SubjectDto;
import com.green.university.entity.Department;
import com.green.university.entity.Subject;
import com.green.university.service.CollegeService;
import com.green.university.service.ProfessorService;
import com.green.university.service.SubjectService;
import com.green.university.utils.Define;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author 서영
 * 강의 목록
 */

@RestController
@RequestMapping("/api/subject")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private ProfessorService professorService;

    // 전체 연도, 학기의 모든 강의 목록 (페이징 + 검색)
    @GetMapping("/list")
    public ResponseEntity<?> readSubjectList(@ModelAttribute AllSubjectSearchFormDto dto,
                                             @PageableDefault(page = 0, size = Define.SUBJECT_PAGE_SIZE, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        // 전체 강의 리스트 불러오기
        Page<SubjectDto> subjectList = subjectService.readSubjectList(dto, pageable);

        Map<String, Object> pagingResponse = new HashMap<>();
        pagingResponse.put("listCount", subjectList.getTotalElements());
        pagingResponse.put("totalPages", subjectList.getTotalPages());
        pagingResponse.put("currentPage", subjectList.getNumber());
        pagingResponse.put("lists", subjectList.getContent());

        return ResponseEntity.ok(pagingResponse);
    }

    // 강의계획서 조회 (활용방법 없을까..?)
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

    // 학생 - 이번 학기 수강/강의 과목 조회
    @GetMapping("/semester")
    public ResponseEntity<?> getSubjectThisSemester(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) Long subjectId
    ) {
        Long id = principal.getId(); // 사용자 아이디 조회

        List<Subject> subjectList = subjectService.getBySubjectNamesByStuSub(id, subjectId);

        return ResponseEntity.ok(Map.of(
                "subjectList", subjectList
        ));
    }



}
