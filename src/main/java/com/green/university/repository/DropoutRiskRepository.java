package com.green.university.repository;

import com.green.university.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface DropoutRiskRepository extends JpaRepository<DropoutRisk, Long> {

    // 특정 학생의 특정 과목에 대한 위험 데이터가 이미 있는지 확인 (중복 생성 방지)
    Optional<DropoutRisk> findByStudent_IdAndSubject_IdAndRiskTypeAndRiskLevel(Long studentId, Long subjectId, RiskType riskType, RiskLevel riskLevel);

    // 교수용: 특정 교수의 과목에서 발생한 위험 학생 리스트
    // (Subject 엔티티에 professor_id가 있다고 가정)
    List<DropoutRisk> findBySubject_Professor_Id(Long professorId);

    // 학생용: 나의 위험 알림 조회
    List<DropoutRisk> findByStudent_IdAndStatusNot(Long studentId, RiskStatus status);

    Optional<DropoutRisk> findById(Long riskId);
    DropoutRisk save(DropoutRisk risk);
    Optional<DropoutRisk> findByStudent_Id(Long StudentId);

    Optional<DropoutRisk> findByStudent_IdAndSubject_IdAndRiskType(Long studentId, Long subjectId, RiskType riskType);
    Optional<DropoutRisk> findByStudent_IdAndSubjectIsNullAndRiskType(Long studentId, RiskType riskType);
    // 같은 학생+과목의 모든 리스크(출석+성적) 같이 가져오기용
    List<DropoutRisk> findByStudent_IdAndSubject_Id(Long studentId, Long subjectId);

    Optional<DropoutRisk> findByStuSubAndRiskType(StuSub stuSub, RiskType riskType);
    List<DropoutRisk> findByStuSub_Student_IdAndStatus(Long studentId, RiskStatus status);
    List<DropoutRisk> findByStuSub_Subject_Professor_IdAndStatus(Long professorId, RiskStatus status);

}
