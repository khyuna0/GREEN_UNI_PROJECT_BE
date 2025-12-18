package com.green.university.domain.evaluation.service;

import com.green.university.domain.evaluation.dto.EvaluationFormDto;
import com.green.university.domain.evaluation.dto.MyEvaluationFormDto;
import com.green.university.domain.evaluation.entity.Evaluation;
import com.green.university.domain.evaluation.repository.EvaluationRepository;
import com.green.university.domain.evaluation.specification.EvaluationSpecification;
import com.green.university.domain.student.entity.Student;
import com.green.university.domain.student.repository.StudentRepository;
import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.domain.subject.repository.StuSubRepository;
import com.green.university.domain.subject.repository.SubjectRepository;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.utils.TermUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
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
    @Autowired
    private StuSubRepository stuSubRepository;

    // 강의 평가 등록 (학생)
    @Transactional
    public void createEvanluation(Long studentId, Long subjectId, EvaluationFormDto evaluationFormDto) {
        // 이미 평가했는지 확인
        boolean exist = evaluationRepository
                .findByStudent_IdAndSubject_Id(studentId, subjectId)
                .isPresent();
        if (exist) {
            throw new CustomRestfullException("이미 해당 과목의 강의평가를 등록했습니다.", HttpStatus.BAD_REQUEST);
        }
        // 학생 , 과목 엔티티 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomRestfullException("학생 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new CustomRestfullException("과목 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 생성자로 생성
        Evaluation evaluation = new Evaluation(student, subject, evaluationFormDto);
        evaluationRepository.save(evaluation);
    }



    // 강의평가 조회 (학생)
    @Transactional
    public Evaluation readEvaluationByStudentIdAndSubjectId(Long studentId, Long subjectId) {
        return evaluationRepository.findByStudent_IdAndSubject_Id(studentId, subjectId)
                .orElseThrow(() -> new CustomRestfullException("강의 평가를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        // .orElse(null);  위처럼 예외던지거나 null처리

    }

    // 전체 강의평가 조회 (교수)
    @Transactional
    public List<MyEvaluationFormDto> readEvaluationByProfessorId(Long professorId) {
        List<Evaluation> evaluations =
                evaluationRepository.findBySubject_Professor_Id(professorId);


        // 밑에 MyEvaluationDto 메서드 선언하면 이렇게 사용
        return evaluations.stream()
                .map(this::toMyEvaluationDto)
                .toList();
    }


    // 과목별 강의평가 조회 (교수)
    @Transactional
    public List<MyEvaluationFormDto> readEvaluationByProfessorIdAndName(Long professorId, String name) {

        Specification<Evaluation> spec =
                Specification.where(EvaluationSpecification.professorIdEq(professorId));

        if (name != null && !name.isBlank()) {
            spec = spec.and(EvaluationSpecification.subjectNameEq(name));
        }

        List<Evaluation> evaluations = evaluationRepository.findAll(spec);

        return evaluations.stream()  // List<Evaluation> → Stream<Evaluation> 으로 바꾸기
                .map(this::toMyEvaluationDto) // this::to~ = e -> this.toMyEvaluationDto(e)
                .toList();
    }

    // 평가가 존재하는 과목 목록 (교수)
    public List<MyEvaluationFormDto> readSubjectName(Long professorId) {
        List<Subject> subjects = evaluationRepository.findDistinctSubjectsByProfessorId(professorId);


        return subjects.stream()
                .map(subject -> {
                    MyEvaluationFormDto dto = new MyEvaluationFormDto();
                    dto.setProfessorId(professorId);
                    dto.setName(subject.getName());
                    return dto;
                })
                .toList();

//        List<MyEvaluationDto> result = new ArrayList<>();
//        for(Subject s : subjects){
//            MyEvaluationDto dto = new MyEvaluationDto();
//            dto.setProfessorId(professorId);
//            dto.setName(s.getName());
//            result.add(dto);
//        }
//
//        return result;
    }


    // ================ 공통 변환 메서드 ===============
    private MyEvaluationFormDto toMyEvaluationDto(Evaluation e) {

        MyEvaluationFormDto dto = new MyEvaluationFormDto();

        if (e.getSubject() != null && e.getSubject().getProfessor() != null) {
            dto.setProfessorId(e.getSubject().getProfessor().getId());
        }

        if (e.getSubject() != null) {
            dto.setName(e.getSubject().getName());
        }

        dto.setAnswer1(e.getAnswer1());
        dto.setAnswer2(e.getAnswer2());
        dto.setAnswer3(e.getAnswer3());
        dto.setAnswer4(e.getAnswer4());
        dto.setAnswer5(e.getAnswer5());
        dto.setAnswer6(e.getAnswer6());
        dto.setAnswer7(e.getAnswer7());
        dto.setImprovements(e.getImprovements());
        dto.calculateAnswerSum();

        return dto;
    }

    // 강의평가 완료 여부
    public boolean isAllEvaluationCompleted(Long studentId) {

        Long year = TermUtil.currentYear();
        Long semester = TermUtil.currentSemester();

        List<StuSub> stuSubs =
                stuSubRepository.findByStudentAndTerm(
                        studentId, year, semester
                );

        // 수강 과목 없으면 평가 대상 아님 → true 처리
        if (stuSubs.isEmpty()) return true;

        for (StuSub stusub : stuSubs) {
            boolean exists = evaluationRepository
                    .existsByStudent_IdAndSubject_Id(
                            studentId,
                            stusub.getSubject().getId()
                    );

            if (!exists) {
                return false; // 하나라도 없으면 바로 false
            }
        }

        return true;
    }

}
