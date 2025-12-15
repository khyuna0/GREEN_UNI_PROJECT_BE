package com.green.university.infra.ai;

import com.green.university.domain.subject.entity.StuSub;
import com.green.university.infra.ai.entity.DropoutRisk;
import com.green.university.infra.ai.entity.RiskStatus;
import com.green.university.infra.ai.entity.RiskType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DropoutRiskRepository extends JpaRepository<DropoutRisk, Long> {

//    // 특정 학생의 특정 과목에 대한 위험 데이터가 이미 있는지 확인 (중복 생성 방지)
//    Optional<DropoutRisk> findByStudent_IdAndSubject_IdAndRiskTypeAndRiskLevel(Long studentId, Long subjectId, RiskType riskType, RiskLevel riskLevel);
//
//    // 교수용: 특정 교수의 과목에서 발생한 위험 학생 리스트
//    // (Subject 엔티티에 professor_id가 있다고 가정)
//    List<DropoutRisk> findBySubject_Professor_Id(Long professorId);
//
//    // 학생용: 나의 위험 알림 조회
//    List<DropoutRisk> findByStudent_IdAndStatusNot(Long studentId, RiskStatus status);
//
//    Optional<DropoutRisk> findById(Long riskId);
//    DropoutRisk save(DropoutRisk risk);
//    Optional<DropoutRisk> findByStudent_Id(Long StudentId);
//
//    Optional<DropoutRisk> findByStudent_IdAndSubject_IdAndRiskType(Long studentId, Long subjectId, RiskType riskType);
//    Optional<DropoutRisk> findByStudent_IdAndSubjectIsNullAndRiskType(Long studentId, RiskType riskType);
//    // 같은 학생+과목의 모든 리스크(출석+성적) 같이 가져오기용
//    List<DropoutRisk> findByStudent_IdAndSubject_Id(Long studentId, Long subjectId);
//
//    List<DropoutRisk> findByStuSub_Subject_Professor_IdAndStatus(Long professorId, RiskStatus status);


    // StuSub + RiskType으로 하나 찾기
    Optional<DropoutRisk> findByStuSubAndRiskType(StuSub stuSub, RiskType riskType);

    // ★ 과목 기준 + 상태로 찾기 (교수 화면용)
    List<DropoutRisk> findByStuSub_Subject_IdAndStatus(Long subjectId, RiskStatus status);

    // 필요하면 학생 기준 조회도 가능
    List<DropoutRisk> findByStuSub_Student_IdAndStatus(Long studentId, RiskStatus status);

    // 상태 필터 없이 전부 가져오고 싶으면 이것도 추가 추천
    List<DropoutRisk> findByStuSub_Subject_Id(Long subjectId);
}