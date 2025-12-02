package com.green.university.repository.interfaces;

import com.green.university.dto.EvaluationDto;
import com.green.university.dto.MyEvaluationDto;
import com.green.university.entity.Evaluation;

import com.green.university.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation,Long> {

    //  엔티티에 studentId, subjectId 필드가 없고
    //  student, subject 객체가 있기 때문에 연관관계 경로 (student.id, subject.id)를 이렇게 씀

    // 특정 과목 이미 평가했는지 조회(학생)
    Optional<Evaluation> findByStudent_IdAndSubject_Id(Long studentId, Long subjectId);

    // 교수 -> 전체 강의 평가 조회
    List<Evaluation> findBySubject_Professor_Id(Long professorId);

    // 교수 + 과목 이름 기준 강의평가 조회
    List<Evaluation> findBySubject_Professor_IdAndSubject_Name(Long professorId, String name);

    // 강의 평가가 존재하는 과목 목록 (중복 제거)
    @Query("select distinct e.subject from Evaluation e where e.subject.professor.id = :professorId")
    List<Subject> findDistinctSubjectsByProfessorId(@Param("professorId") Long professorId);

}
