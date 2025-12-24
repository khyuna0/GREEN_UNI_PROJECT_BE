package com.green.university.domain.counseling.repository;

import com.green.university.domain.counseling.entity.ApprovalState;
import com.green.university.domain.counseling.entity.CounselingReserve;
import com.green.university.domain.counseling.entity.ReserveRequester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CounselingReserveRepository extends JpaRepository<CounselingReserve, Long> {

    // ===================== 공통 조회 =====================

    List<CounselingReserve> findByCounselingSchedule_Id(Long counselingScheduleId);

    List<CounselingReserve> findByStudent_Id(Long studentId);

    List<CounselingReserve> findByCounselingSchedule_Professor_Id(Long professorId);

    int countByStudent_IdAndApprovalState(Long studentId, ApprovalState approvalState);

    // 교수 미처리 상담(학생 신청만) 카운트
    int countByCounselingSchedule_Professor_IdAndApprovalStateAndRequester(
            Long professorId,
            ApprovalState approvalState,
            ReserveRequester requester
    );

    // ===================== 중복 방지(개별 과목/슬롯) =====================

    boolean existsByStudent_IdAndSubject_IdAndApprovalStateAndRequester(
            Long studentId,
            Long subjectId,
            ApprovalState approvalState,
            ReserveRequester requester
    );

    boolean existsByStudent_IdAndCounselingSchedule_IdAndApprovalStateIn(
            Long studentId,
            Long counselingScheduleId,
            List<ApprovalState> states
    );

    boolean existsByStudent_IdAndCounselingSchedule_IdAndRequesterAndApprovalState(
            Long studentId,
            Long counselingScheduleId,
            ReserveRequester requester,
            ApprovalState approvalState
    );

    // ===================== 위험과목 상담 동기화(개별 DropoutRisk) =====================

    boolean existsByDropoutRisk_IdAndApprovalStateIn(
            Long dropoutRiskId,
            List<ApprovalState> states
    );

    Optional<CounselingReserve> findTop1ByDropoutRisk_IdOrderByIdDesc(Long dropoutRiskId);

    // ===================== 교수요청(학생에게 온 요청) =====================

    List<CounselingReserve> findByStudent_IdAndRequesterAndApprovalState(
            Long studentId,
            ReserveRequester requester,
            ApprovalState approvalState
    );

    // 학생 기준 최신 교수요청 1건
    Optional<CounselingReserve> findTop1ByStudent_IdAndRequesterOrderByIdDesc(
            Long studentId,
            ReserveRequester requester
    );

    boolean existsByCounselingSchedule_Id(Long counselingScheduleId);


    // 학생 아이디, 방 번호, 예약 상태, 시간으로 유효한 방 조회
    boolean existsByStudent_IdAndRoomCodeAndApprovalStateAndCounselingSchedule_StartTimeAndCounselingSchedule_EndTimeAndCounselingSchedule_CounselingDate(
            Long studentId,
            String roomCode,
            ApprovalState approvalState,
            Long now,
            Long now2,
            LocalDate date

    );

    // 교수의 과목 코드 리스트, 방 번호, 예약 상태, 시간으로 유효한 방 조회
    boolean existsBySubject_IdInAndRoomCodeAndApprovalStateAndCounselingSchedule_StartTimeAndCounselingSchedule_EndTimeAndCounselingSchedule_CounselingDate(
            List<Long> subjectIds,
            String roomCode,
            ApprovalState approvalState,
            Long now,
            Long now2,
            LocalDate date
    );


    // 룸코드, 상담 예약 승인 상태로 방 조회
    CounselingReserve findByRoomCodeAndApprovalState(String roomCode, ApprovalState approvalState);

    // 로그인한 유저 + Requester 따라 상담 내역 조회
    List<CounselingReserve> findBySubject_IdIn(List<Long> subjectIds);
}
