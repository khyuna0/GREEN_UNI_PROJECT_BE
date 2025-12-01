package com.green.university.service;

import com.green.university.dto.EvaluationDto;
import com.green.university.dto.MyEvaluationDto;
import com.green.university.entity.Student;
import com.green.university.entity.Subject;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.repository.interfaces.EvaluationRepository;
import com.green.university.entity.Evaluation;
import com.green.university.repository.interfaces.StudentRepository;
import com.green.university.repository.interfaces.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvaluationService {

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    // 강의 평가 등록 (학생)
    @Transactional
    public void createEvanluation(EvaluationDto evaluationFormDto) {

       Long studentId = evaluationFormDto.getStudentId();
       Long subjectId = evaluationFormDto.getSubjectId();

       // 이미 평가했는지 확인
        boolean exist = evaluationRepository
                .findByStudent_IdAndSubject_Id(studentId,subjectId)
                .isPresent();

        if(exist){
            throw new CustomRestfullException("이미 해당 과목의 강의평가를 등록했습니다.", HttpStatus.BAD_REQUEST);
        }

        // 학생 , 과목 엔티티 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(()-> new CustomRestfullException("학생 정보를 찾을 수 없습니다.",HttpStatus.NOT_FOUND));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(()-> new CustomRestfullException("과목 정보를 찾을 수 없습니다.",HttpStatus.NOT_FOUND));

        // 엔티티 매핑
        Evaluation evaluation = new Evaluation();
        evaluation.setStudent(student);
        evaluation.setSubject(subject);
        evaluation.setAnswer1(evaluationFormDto.getAnswer1());
        evaluation.setAnswer2(evaluationFormDto.getAnswer2());
        evaluation.setAnswer3(evaluationFormDto.getAnswer3());
        evaluation.setAnswer4(evaluationFormDto.getAnswer4());
        evaluation.setAnswer5(evaluationFormDto.getAnswer5());
        evaluation.setAnswer6(evaluationFormDto.getAnswer6());
        evaluation.setAnswer7(evaluationFormDto.getAnswer7());

        evaluationRepository.save(evaluation);

    }

    // 강의평가 조회 (학생)
    @Transactional
    public Evaluation readEvaluationByStudentIdAndSubjectId(Long studentId) {
        Evaluation evaluation = evaluationRepository.selectEvaluation(studentId);
        return evaluation;
    }

    // 전체 강의평가 조회 (교수)
    @Transactional
    public List<MyEvaluationDto> readEvaluationByProfessorId(Long professorId) {
        List<MyEvaluationDto> evaluation = evaluationRepository.selectMyEvaluationDtoByProfessorId(professorId);
        return evaluation;
    }

    // 과목별 강의평가 조회 (교수)
    @Transactional
    public List<MyEvaluationDto> readEvaluationByProfessorIdAndName(Long professorId, String name) {
        List<MyEvaluationDto> evaluation = evaluationRepository.selectEvaluationDtoByprofessorIdAndName(professorId,
                name);
        return evaluation;
    }

    public List<MyEvaluationDto> readSubjectName(Long professorId) {
        List<MyEvaluationDto> subjectName = evaluationRepository.selectEvaluationDto(professorId);
        return subjectName;
    }
}
