package com.green.university.domain.counseling.dto;


import com.green.university.domain.counseling.entity.ApprovalState;
import com.green.university.domain.counseling.entity.CounselingReserve;
import com.green.university.domain.counseling.entity.CounselingSchedule;
import com.green.university.domain.dropoutrisk.entity.DropoutRisk;
import com.green.university.domain.student.entity.Student;
import com.green.university.domain.subject.entity.Subject;
import lombok.Data;

@Data
public class CounselingReserveDto {

    private Long id;

    // 학생
    private Student student;

    // 교수 오픈 일정
    private CounselingSchedule counselingSchedule;

    // 해당 과목
    private Subject subject;

    // 화상 상담 방 코드
    private String roomCode;

    // 학생 위험 상태 - 위험학생 아니면 null
    private DropoutRisk dropoutRisk;

    // 상담 사유
    private String reason;

    // 승인 여부
    private ApprovalState approvalState;

    public CounselingReserveDto(CounselingReserve entity) {
        this.id = entity.getId();
        this.student = entity.getStudent();
        this.subject = entity.getSubject();
        this.counselingSchedule = entity.getCounselingSchedule();
        this.roomCode = entity.getRoomCode();
        this.dropoutRisk = entity.getDropoutRisk();
        this.reason = entity.getReason();
        this.approvalState = entity.getApprovalState();
    }

}
