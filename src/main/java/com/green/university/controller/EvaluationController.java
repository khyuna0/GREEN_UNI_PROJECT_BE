package com.green.university.controller;

import com.green.university.dto.EvaluationDto;
import com.green.university.dto.MyEvaluationDto;
import com.green.university.dto.response.QuestionDto;
import com.green.university.exception.CustomRestfullException;
import com.green.university.config.security.CustomUserDetails;
import com.green.university.service.EvaluationService;
import com.green.university.service.QuestionService;
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
    public ResponseEntity<?> EvaluationProc(@PathVariable("subjectId") Long subjectId, EvaluationDto evaluationFormDto,
                                            @AuthenticationPrincipal CustomUserDetails principal) {

        Long studentId = principal.getId();

        evaluationFormDto.setStudentId(studentId);
        evaluationFormDto.setSubjectId(subjectId);

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
            evaluationService.createEvanluation(evaluationFormDto);
        }

//		// 창을 닫을 때 post가 작동이 안하는거 방지
//		model.addAttribute("type", 1);
        return ResponseEntity.ok().body("강의평가가 완료되었습니다");
    }

    // 강의 평가 처음화면 (교수)
    @GetMapping("/read")
    public ResponseEntity<?> readEvaluation(@AuthenticationPrincipal CustomUserDetails principal) {

        Long professorId = principal.getId();

        List<MyEvaluationDto> subjectName = evaluationService.readSubjectName(professorId);
        List<MyEvaluationDto> eval = evaluationService.readEvaluationByProfessorId(professorId);

        return ResponseEntity.ok(Map.of(
                "subjectName", subjectName,
                "eval", eval
        ));
    }

    // 과목별 강의 평가 조회 (교수)
    @PostMapping("/read/{subject_Name}")
    public ResponseEntity<?> readEvaluation( @RequestParam("subject_Name") String subject_Name,   // 기존 request.getParameter("subjectId") 대체
                                             @AuthenticationPrincipal CustomUserDetails principal) {

        Long professorId = principal.getId();
        List<MyEvaluationDto> subjectName = evaluationService.readSubjectName(professorId);
        List<MyEvaluationDto> eval = evaluationService.readEvaluationByProfessorIdAndName(professorId, subject_Name);
        return ResponseEntity.ok(Map.of(
                "subjectName", subjectName,
                "eval", eval
        ));
    }

}
