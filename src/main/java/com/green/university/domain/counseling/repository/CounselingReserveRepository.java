package com.green.university.domain.counseling.repository;

import com.green.university.domain.counseling.entity.CounselingReserve;
import com.green.university.domain.counseling.entity.ApprovalState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounselingReserveRepository extends JpaRepository<CounselingReserve, Long> {

    // 학생 기준 상담 예약 목록 조회
    List<CounselingReserve> findByStudentId(Long studentId);

    // 교수 기준 상담 예약 목록 조회
    // CounselingSchedule -> Professor 연관관계를 통해 조회
    List<CounselingReserve> findByCounselingSchedule_Professor_Id(Long professorId);

    // 특정 상담 일정에 대한 모든 예약 조회
    // 승인 시 같은 일정의 다른 예약 반려
    List<CounselingReserve> findByCounselingSchedule_Id(Long counselingScheduleId);

    // 상태 기준 조회
    // 필요 시 REQUESTED / APPROVED / REJECTED 필터링 용도
    List<CounselingReserve> findByApprovalState(ApprovalState approvalState);

    // 같은 학생 + 같은 상담 일정 예약 존재 여부
    boolean existsByStudent_IdAndCounselingSchedule_Id(Long studentId, Long counselingScheduleId);

    // 과목 ID 목록 기준 미처리 상담 신청 목록
    List<CounselingReserve>
    findBySubject_IdInAndApprovalState(
            List<Long> subjectIds,
            ApprovalState approvalState
    );
}
