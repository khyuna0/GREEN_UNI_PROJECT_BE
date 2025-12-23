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

    // REJECTED는 재신청 가능하게 하려면 "REQUESTED/APPROVED만" 막는 게 안전
    boolean existsByStudent_IdAndCounselingSchedule_IdAndApprovalStateIn(
            Long studentId,
            Long counselingScheduleId,
            List<ApprovalState> states
    );

    // 같은 학생 + 같은 과목 기준 "REQUESTED" 중복 방지 (학생발/교수발 구분)
    // 이거 없으면 학생이 상담신청 무한으로 가능
    boolean existsByStudent_IdAndSubject_IdAndApprovalStateAndRequester(
            Long studentId,
            Long subjectId,
            ApprovalState approvalState,
            ReserveRequester requester
    );

    // 교수 화면 상담요청: 학생 신청만 보이게 처리 -> 프론트 필터로도 가능
    List<CounselingReserve> findByCounselingSchedule_Professor_IdAndApprovalStateAndRequester(
            Long professorId,
            ApprovalState approvalState,
            ReserveRequester requester
    );

    // 이미 예약 신청된 예약이 있는지 확인
    boolean existsByCounselingSchedule_Id(Long counselingScheduleId);

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


    // 학생 기준 가장 최근 교수요청 1건 가져오기 (담당교수 표시용)
    Optional<CounselingReserve> findTop1ByStudent_IdAndRequesterOrderByIdDesc(
            Long studentId,
            ReserveRequester requester
    );

    // 학생 아이디, 방 번호, 예약 상태로 유효한 방 조회
    boolean existsByStudent_IdAndRoomCodeAndApprovalState(
            Long studentId,
            String roomCode,
            ApprovalState approvalState
    );

    // 교수의 과목 코드 리스트, 방 번호, 예약 상태로 유효한 방 조회
    boolean existsBySubject_IdInAndRoomCodeAndApprovalState(
            List<Long> subjectId,
            String roomCode,
            ApprovalState approvalState
    );

    // 룸코드, 예약 상태로 방 조회
    CounselingReserve findByRoomCodeAndApprovalState(String roomCode, ApprovalState approvalState);
}
