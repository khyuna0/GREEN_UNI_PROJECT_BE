package com.green.university.domain.dropoutrisk.dto;

import com.green.university.domain.counseling.entity.ApprovalState;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DropoutStudentRiskRowDto {

    private Long studentId;
    private String studentName;

    private int dangerCount;   // DANGER 과목 수
    private int warningCount;  // WARNING 과목 수

    private String overallLevel; // "DANGER" | "WARNING" | "NORMAL"
    private String reason;       // 화면용 요약 (예: "DANGER 2, WARNING 1")

    private LocalDateTime updatedAt; // 학생 기준 최신 업데이트

    // 담당(지정) 교수 표시용 , 학과 교수 2명 모두에게 노출
    private Long assignedProfessorId;
    private String assignedProfessorName;
    private LocalDateTime assignedAt;


}
