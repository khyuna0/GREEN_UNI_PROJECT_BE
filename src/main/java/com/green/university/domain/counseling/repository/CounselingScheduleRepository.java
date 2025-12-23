package com.green.university.domain.counseling.repository;

import com.green.university.domain.counseling.entity.CounselingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CounselingScheduleRepository extends JpaRepository<CounselingSchedule, Long> {

    // 내 상담 일정 불러오기 (교수)
    List<CounselingSchedule> findByProfessor_IdAndCounselingDateBetween(
            Long professorId,
            LocalDate start,
            LocalDate end
    );

    // 내가 열어 둔 상담 일정 보기
    boolean existsByProfessor_IdAndCounselingDateAndStartTime(
            Long professorId,
            LocalDate counselingDate,
            Long startTime
    );

    CounselingSchedule findByProfessor_IdAndCounselingDateAndStartTime(Long professorId, LocalDate date, Long startTime);

    // 교수 기준 예약 완료된 상담 일정 조회
    List<CounselingSchedule> findByProfessor_IdAndReserved(Long professorId, boolean reserved);

    // 날짜, 시간 기준 미래의 상담 시간만
    List<CounselingSchedule> findByProfessor_IdAndReservedFalseAndCounselingDateGreaterThanEqualOrderByCounselingDateAscStartTimeAsc(Long professorId, LocalDate now);

    List<CounselingSchedule> findByProfessor_IdAndCounselingDateAndReserved(Long professorId, LocalDate today, boolean reserved);

}
