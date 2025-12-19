package com.green.university.domain.dropoutrisk.entity;

import com.green.university.domain.subject.entity.StuSub;
import com.green.university.infra.ai.dto.AiRiskAnalysisResult;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DropoutRisk { // 위험 학생 관리 테이블

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 학생, 어떤 과목(또는 학기) 기준인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stu_sub_id", nullable = false)
    private StuSub stuSub;


    @Enumerated(EnumType.STRING)
    private RiskType riskType; // ATTENDANCE 출석, SUBJECT_GRADE, SEMESTER_GPA

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel; // WARNING 경고(결석2회), DANGER 위험(결석3회/학점미달)

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RiskStatus status = RiskStatus.DETECTED; // DETECTED 포착됨, CONSULT_REQ 상담예약, RESOLVED 해결완료


    // AI에게 넘긴 핵심 요약
    @Column(columnDefinition = "TEXT", nullable = false)
    private String lastAiInput;

    // AI가 만든 한 줄 요약 (교수/학생 공통)
    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    // 교수 상담 가이드
    @Column(columnDefinition = "TEXT")
    private String aiRecommendation;

    // 학생에게 보여줄 메시지 (동기 부여, 학습 전략)
    @Column(columnDefinition = "TEXT")
    private String aiStudentMessage;

    // 위험 요인 태그 (CSV 또는 JSON 문자열)
    @Column(columnDefinition = "TEXT")
    private String aiReasonTags;


    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    // 편의 메서드
    public void updateFromAiResult(AiRiskAnalysisResult result) {
        this.aiSummary = result.getSummary();
        this.aiRecommendation = result.getProfessorGuide();
        this.aiStudentMessage = result.getStudentMessage();
        this.aiReasonTags = String.join(",", result.getReasonTags());
    }

}