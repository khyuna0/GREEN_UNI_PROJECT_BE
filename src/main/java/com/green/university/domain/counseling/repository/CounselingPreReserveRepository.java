package com.green.university.domain.counseling.repository;

import com.green.university.domain.counseling.entity.ApprovalState;
import com.green.university.domain.counseling.entity.CounselingReserve;
import com.green.university.domain.counseling.entity.ReserveRequester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounselingPreReserveRepository extends JpaRepository<CounselingReserve, Long> {

    // 학생이 받은 교수의 상담 요청 목록
    List<CounselingReserve> findByStudent_IdAndApprovalState(Long studentId, ApprovalState state);

    // 중복 요청방지
    boolean existsByStudent_IdAndCounselingSchedule_IdAndApprovalState(
            Long studentId,
            Long counselingScheduleId,
            ApprovalState approvalState
    );

    // 교수요청만 조회 (requester=PROFESSOR)
    List<CounselingReserve> findByStudent_IdAndRequesterAndApprovalState(
            Long studentId,
            ReserveRequester requester,
            ApprovalState state
    );

    // 교수요청 중복 방지 (REQUESTED만 막고, REJECTED는 재요청 가능)
    boolean existsByStudent_IdAndCounselingSchedule_IdAndRequesterAndApprovalState(
            Long studentId,
            Long counselingScheduleId,
            ReserveRequester requester,
            ApprovalState approvalState
    );
}
