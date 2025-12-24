package com.green.university.domain.dropoutrisk.dto;

import com.green.university.domain.dropoutrisk.entity.DropoutRisk;
import com.green.university.domain.dropoutrisk.entity.RiskLevel;
import com.green.university.domain.dropoutrisk.entity.RiskStatus;
import com.green.university.domain.dropoutrisk.entity.RiskType;
import com.green.university.global.utils.DateTimeUtil;
import lombok.Data;

@Data
public class DropoutRiskResponseDto {

    private Long id;

    private Long studentId;
    private String studentName;

    private Long subjectId;
    private String subjectName;

    private Long subjectProfessorId;   //  내 과목 판별용
    private String professorName;

    private RiskType riskType;
    private RiskLevel riskLevel;
    private RiskStatus status;

    private String aiSummary;
    private String aiRecommendation;
    private String aiStudentMessage;
    private String aiReasonTags;

    private String createdAt;
    private String updatedAt;

    // 교수요청/거절 표시용 (enum 추가 없이 UI에서 사용)
    // 예: "CONSULT_REQ" / "CONSULT_REJECTED" / null
    private String consultState;

    private boolean mySubject; // 내 과목이면 상담신청 가능 표시

    public static DropoutRiskResponseDto fromEntity(DropoutRisk risk) {
        DropoutRiskResponseDto dto = new DropoutRiskResponseDto();
        dto.setId(risk.getId());

        dto.setStudentId(risk.getStuSub().getStudent().getId());
        dto.setStudentName(risk.getStuSub().getStudent().getName());

        dto.setSubjectId(risk.getStuSub().getSubject().getId());
        dto.setSubjectName(risk.getStuSub().getSubject().getName());

        if (risk.getStuSub().getSubject().getProfessor() != null) {
            dto.setSubjectProfessorId(risk.getStuSub().getSubject().getProfessor().getId());
            dto.setProfessorName(risk.getStuSub().getSubject().getProfessor().getName());
        }

        dto.setRiskType(risk.getRiskType());
        dto.setRiskLevel(risk.getRiskLevel());
        dto.setStatus(risk.getStatus());

        dto.setAiSummary(risk.getAiSummary());
        dto.setAiRecommendation(risk.getAiRecommendation());
        dto.setAiStudentMessage(risk.getAiStudentMessage());
        dto.setAiReasonTags(risk.getAiReasonTags());

        dto.setCreatedAt(DateTimeUtil.dateTimeToString(risk.getCreatedAt()));
        dto.setUpdatedAt(DateTimeUtil.dateTimeToString(risk.getUpdatedAt()));
        return dto;
    }

    // consultState까지 세팅하는 오버로드 (기존 코드 호환 유지)
    public static DropoutRiskResponseDto fromEntity(DropoutRisk risk, String consultState) {
        DropoutRiskResponseDto dto = fromEntity(risk);
        dto.setConsultState(consultState);
        return dto;
    }

    // consultState + mySubject까지 세팅
    public static DropoutRiskResponseDto fromEntity(DropoutRisk risk, String consultState, boolean mySubject) {
        DropoutRiskResponseDto dto = fromEntity(risk, consultState);
        dto.setMySubject(mySubject);
        return dto;
    }
}
