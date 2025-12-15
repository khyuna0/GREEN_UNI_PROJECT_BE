package com.green.university.domain.evaluation.service;

import com.green.university.domain.evaluation.dto.EvaluationFormDto;
import com.green.university.domain.evaluation.dto.MyEvaluationFormDto;
import com.green.university.domain.student.entity.Student;
import com.green.university.domain.subject.entity.Subject;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.domain.evaluation.repository.EvaluationRepository;
import com.green.university.domain.evaluation.entity.Evaluation;
import com.green.university.domain.student.repository.StudentRepository;
import com.green.university.domain.subject.repository.SubjectRepository;
import com.green.university.domain.evaluation.specification.EvaluationSpecification;
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

    // 강의 평가 등록 (학생)
    @Transactional
    public void createEvanluation(Long studentId, Long subjectId, EvaluationFormDto evaluationFormDto) {


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
    public Evaluation readEvaluationByStudentIdAndSubjectId(Long studentId, Long subjectId) {
        return evaluationRepository.findByStudent_IdAndSubject_Id(studentId, subjectId)
                .orElseThrow(() -> new CustomRestfullException("강의 평가를 찾을 수 없습니다.",HttpStatus.NOT_FOUND));
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


        // 메서드 없이 직접 넣을땐
        //        List<MyEvaluationDto> result = new ArrayList<>();
//        for (Evaluation e : evaluations) {
//            MyEvaluationDto dto = new MyEvaluationDto();
//
//            // 교수 id 채우기
//            if (e.getSubject() != null && e.getSubject().getProfessor() != null) {
//                dto.setProfessorId(e.getSubject().getProfessor().getId());
//            } else {
//                dto.setProfessorId(null); // 혹시 몰라서 방어 코드
//            }
//
//            // 과목 이름 (MyEvaluationDto의 name 필드를 과목명으로 쓴다고 가정)
//            if (e.getSubject() != null) {
//                dto.setName(e.getSubject().getName()); // Subject 엔티티에 getName() 이 있어야 함
//            }
//
//            // 점수들
//            dto.setAnswer1(e.getAnswer1());
//            dto.setAnswer2(e.getAnswer2());
//            dto.setAnswer3(e.getAnswer3());
//            dto.setAnswer4(e.getAnswer4());
//            dto.setAnswer5(e.getAnswer5());
//            dto.setAnswer6(e.getAnswer6());
//            dto.setAnswer7(e.getAnswer7());
//
//            // 개선사항
//            dto.setImprovements(e.getImprovements());
//
//            // 리스트에 추가
//            result.add(dto);
//        }
//
//        return result;

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


    // 공통 변환 메서드  toMyEvaluationDto(e) 쓰는경우
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
}
