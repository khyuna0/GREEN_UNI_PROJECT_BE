package com.green.university.domain.counseling.repository;

import com.green.university.domain.counseling.entity.ApprovalState;
import com.green.university.domain.counseling.entity.CounselingPreReserve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounselingPreReserveRepository extends JpaRepository<CounselingPreReserve, Long> {

    // 학생이 받은 교수의 상담 요청 목록
    List<CounselingPreReserve> findByStudent_IdAndApprovalState(Long studentId, ApprovalState state);

    // 학생이 받은 교수의 상담 요청 목록 정렬
    List<CounselingPreReserve> findByStudent_IdAndApprovalStateOrderByCounselingSchedule_CounselingDateAscCounselingSchedule_StartTimeAsc(
            Long studentId,
            ApprovalState state
    );

    // 중복 요청방지
    boolean existsByStudent_IdAndCounselingSchedule_IdAndApprovalState(
            Long studentId,
            Long counselingScheduleId,
            ApprovalState approvalState
    );
}
