package com.green.university.domain.evaluation.controller;

import com.green.university.domain.evaluation.dto.EvaluationFormDto;
import com.green.university.domain.evaluation.dto.MyEvaluationFormDto;
import com.green.university.domain.subject.dto.QuestionDto;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.security.CustomUserDetails;
import com.green.university.domain.evaluation.service.EvaluationService;
import com.green.university.domain.subject.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private HttpSession session;
    @Autowired
    private EvaluationService evaluationService;
    @Autowired
    private QuestionService questionService;

    /**
     * 편용림
     *
     * @return 강의평가 화면 클릭
     */
    @GetMapping
    public ResponseEntity<?> evaluation(Long subjectId) {

        QuestionDto questionDto = questionService.readQuestion();
        return ResponseEntity.ok(Map.of(
                "subjectId", subjectId,
                "questionDto", questionDto
        ));
    }

    /*
     * 강의평가 post
     */
    @PostMapping("/write/{subjectId}")
    public ResponseEntity<?> EvaluationProc(@PathVariable("subjectId") Long subjectId,
                                            @RequestBody EvaluationFormDto evaluationFormDto,
                                            @AuthenticationPrincipal CustomUserDetails principal) {

        Long studentId = principal.getId();

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
            evaluationService.createEvanluation(studentId,subjectId,evaluationFormDto);
        }

//		// 창을 닫을 때 post가 작동이 안하는거 방지
//		model.addAttribute("type", 1);
        return ResponseEntity.ok().body("강의평가가 완료되었습니다");
    }

//    // 강의 평가 처음화면 (교수)
//    @GetMapping("/read")
//    public ResponseEntity<?> readEvaluation(@AuthenticationPrincipal CustomUserDetails principal) {
//
//        Long professorId = principal.getId();
//
//        List<MyEvaluationDto> subjectName = evaluationService.readSubjectName(professorId);
//        List<MyEvaluationDto> eval = evaluationService.readEvaluationByProfessorId(professorId);
//
//        return ResponseEntity.ok(Map.of(
//                "subjectName", subjectName,
//                "eval", eval
//        ));
//    }

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

}
