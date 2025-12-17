package com.green.university.domain.counseling.dto;

import com.green.university.domain.counseling.entity.ApprovalState;
import com.green.university.domain.counseling.entity.CounselingPreReserve;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CounselPreReserveDto {
    // 학생이 보는 교수의 상담 요청

    private Long preReserveId;

    private Long subjectId;
    private String subjectName;

    private Long counselingScheduleId;
    private String professorName;

    private LocalDate counselingDate;
    private Long startTime;
    private Long endTime;

    private String reason;
    private ApprovalState approvalState;

    public CounselPreReserveDto(CounselingPreReserve pre) {
        this.preReserveId = pre.getId();
        this.reason = pre.getReason();
        this.approvalState = pre.getApprovalState();

        // subject
        if (pre.getSubject() != null) {
            this.subjectId = pre.getSubject().getId();
            this.subjectName = pre.getSubject().getName();
        }

        // schedule + professor
        if (pre.getCounselingSchedule() != null) {
            this.counselingScheduleId = pre.getCounselingSchedule().getId();
            this.counselingDate = pre.getCounselingSchedule().getCounselingDate();
            this.startTime = pre.getCounselingSchedule().getStartTime();
            this.endTime = pre.getCounselingSchedule().getEndTime();

            if (pre.getCounselingSchedule().getProfessor() != null) {
                this.professorName = pre.getCounselingSchedule().getProfessor().getName();
            }
        }
    }
}
