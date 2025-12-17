package com.green.university.domain.evaluation.controller;

import com.green.university.domain.evaluation.dto.EvaluationFormDto;
import com.green.university.domain.evaluation.dto.MyEvaluationFormDto;
import com.green.university.domain.evaluation.service.EvaluationService;
import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.service.StuSubService;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;
    @Autowired
    private StuSubService stuSubService;


    // 학생이 하는 강의평가 post
    @PostMapping("/write/{subjectId}")
    public ResponseEntity<?> EvaluationProc(@PathVariable("subjectId") Long subjectId,
                                            @RequestBody EvaluationFormDto evaluationFormDto,
                                            @AuthenticationPrincipal CustomUserDetails principal) {
        Long studentId = principal.getId();
        Optional<StuSub> stuSub = stuSubService.readStuSub(studentId, subjectId);
        if (stuSub.isEmpty()) {
            throw new CustomRestfullException("해당 학생의 해당 과목 수강 내역이 존재하지 않습니다", HttpStatus.NOT_FOUND);
        }

        if (evaluationFormDto.getAnswer1() == null) {
            throw new CustomRestfullException("1번 질문에 답 해주세요", HttpStatus.BAD_REQUEST);
        } else if (evaluationFormDto.getAnswer2() == null) {
            throw new CustomRestfullException("2번 질문에 답 해주세요", HttpStatus.BAD_REQUEST);
        } else if (evaluationFormDto.getAnswer3() == null) {
            throw new CustomRestfullException("3번 질문에 답 해주세요", HttpStatus.BAD_REQUEST);
        } else if (evaluationFormDto.getAnswer4() == null) {
            throw new CustomRestfullException("4번 질문에 답 해주세요", HttpStatus.BAD_REQUEST);
        } else if (evaluationFormDto.getAnswer5() == null) {
            throw new CustomRestfullException("5번 질문에 답 해주세요", HttpStatus.BAD_REQUEST);
        } else if (evaluationFormDto.getAnswer6() == null) {
            throw new CustomRestfullException("6번 질문에 답 해주세요", HttpStatus.BAD_REQUEST);
        } else if (evaluationFormDto.getAnswer7() == null) {
            throw new CustomRestfullException("7번 질문에 답 해주세요", HttpStatus.BAD_REQUEST);
        } else {
            evaluationService.createEvanluation(studentId, subjectId, evaluationFormDto);
        }
        return ResponseEntity.ok().body("강의평가가 완료되었습니다");
    }

    // 과목별 강의 평가 조회 (교수)
    @GetMapping("/read")
    public ResponseEntity<?> readEvaluation(@RequestParam(required = false, value = "subject_Name") String subject_Name,
                                            @AuthenticationPrincipal CustomUserDetails principal) {
        Long professorId = principal.getId();
        List<MyEvaluationFormDto> subjectName = evaluationService.readSubjectName(professorId);
        List<MyEvaluationFormDto> eval = evaluationService.readEvaluationByProfessorIdAndName(professorId, subject_Name);
        return ResponseEntity.ok(Map.of(
                "subNames", subjectName,
                "eval", eval
        ));
    }

    // 이번 학기 수강 과목 강의평가 완료 여부 확인 - 유효성 체크 용
    @GetMapping("/hasEval")
    public ResponseEntity<?> hasEval(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if(!Objects.equals(principal.getUserRole(), "student")) {
            throw new CustomRestfullException("접근 권한이 없습니다.", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(Map.of(
                "hasEval", evaluationService.isAllEvaluationCompleted(principal.getId())
        ));
    }

}
