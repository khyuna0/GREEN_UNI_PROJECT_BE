package com.green.university.dto.response;

import com.green.university.entity.*;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class CounselingPreReserveDto {

    private Long id;

    private Student student; // 대상 학생
    
    private Subject subject; // 대상 과목 (Name과 아이디만 저장?)

    // 교수 오픈 일정 (여기에 교수 정보 저장되어 있음)
    private CounselingSchedule counselingSchedule;

    // 상담 사유
    private String reason;

    // 학생 위험 상태 / 위험학생 아니면 null
    private DropoutRisk dropoutRisk;

    // 승인 여부
    private ReserveStatus approvalState;
    // 학생 신청, 교수 승인(승인 시 예약 생성), 반려


    public CounselingPreReserveDto(CounselingPreReserve entity) {
        this.id = entity.getId();
        this.student = entity.getStudent();
        this.subject = entity.getSubject();
        this.counselingSchedule = entity.getCounselingSchedule();
        this.reason = entity.getReason();
        this.dropoutRisk = entity.getDropoutRisk();
        this.approvalState = entity.getApprovalState();
    }
}
