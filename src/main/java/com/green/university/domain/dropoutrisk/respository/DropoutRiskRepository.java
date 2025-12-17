package com.green.university.domain.dropoutrisk.respository;

import com.green.university.domain.dropoutrisk.entity.RiskLevel;
import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.dropoutrisk.entity.DropoutRisk;
import com.green.university.domain.dropoutrisk.entity.RiskStatus;
import com.green.university.domain.dropoutrisk.entity.RiskType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DropoutRiskRepository extends JpaRepository<DropoutRisk, Long> {

    // StuSub + RiskType으로 하나 찾기
    Optional<DropoutRisk> findByStuSubAndRiskType(StuSub stuSub, RiskType riskType);

    // ★ 과목 기준 + 상태로 찾기 (교수 화면용)
    List<DropoutRisk> findByStuSub_Subject_IdAndStatus(Long subjectId, RiskStatus status);

    // 필요하면 학생 기준 조회도 가능
    List<DropoutRisk> findByStuSub_Student_IdAndStatus(Long studentId, RiskStatus status);

    // 상태 필터 없이 전부 가져오고 싶으면 이것도 추가 추천
    List<DropoutRisk> findByStuSub_Subject_Id(Long subjectId);

    Optional<DropoutRisk> findByStuSubId(Long id);

    // StuSub 기준으로 위험 학생 조회
    Optional<DropoutRisk> findByStuSub_Id(Long stuSubId);

    List<DropoutRisk> findByStatus(RiskStatus status);
    List<DropoutRisk> findByStatusIn(List<RiskStatus> status);

    List<DropoutRisk> findByStuSub_Subject_IdAndRiskLevel(Long stuSubSubjectId, RiskLevel riskLevel);

    List<DropoutRisk> findByRiskLevel(RiskLevel riskLevel);

    List<DropoutRisk> findByStuSub_Subject_Professor_Id(Long professorId);
    List<DropoutRisk> findByStuSub_Subject_Professor_IdAndRiskLevel(
            Long professorId,
            RiskLevel riskLevel
    );
}
