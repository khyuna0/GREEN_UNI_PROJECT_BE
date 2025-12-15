package com.green.university.domain.subject.controller;

import com.green.university.domain.subject.dto.AllSubjectSearchFormDto;
import com.green.university.domain.professor.dto.ReadSyllabusDto;
import com.green.university.domain.subject.dto.SubjectDto;
import com.green.university.domain.professor.service.ProfessorService;
import com.green.university.domain.subject.service.SubjectService;
import com.green.university.global.utils.Define;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

}
