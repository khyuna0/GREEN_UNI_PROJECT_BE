package com.green.university.domain.counseling.repository;

import com.green.university.domain.counseling.entity.CounselingReserve;
import com.green.university.domain.counseling.entity.ApprovalState;
import com.green.university.domain.counseling.entity.ReserveRequester;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

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
    List<CounselingReserve> findBySubject_IdInAndApprovalState(
            List<Long> subjectIds,
            ApprovalState approvalState
    );

    // 학생 상담 알림용 카운트
    int countByStudent_IdAndApprovalState(Long studentId, ApprovalState approvalState);

    // ===================== 교수 -> 학생 요청(PreReserve) 통합 =====================

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

    // 교수요청 최신 1건 조회
    Optional<CounselingReserve> findTop1ByStudent_IdAndSubject_IdAndRequesterOrderByIdDesc(
            Long studentId,
            Long subjectId,
            ReserveRequester requester
    );
}
