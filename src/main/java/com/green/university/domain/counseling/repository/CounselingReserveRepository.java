package com.green.university.domain.counseling.repository;

import com.green.university.domain.counseling.dto.CounselingReserveDto;
import com.green.university.domain.counseling.entity.CounselingReserve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounselingReserveRepository extends JpaRepository<CounselingReserve, Long> {

    List<CounselingReserve> findByStudentId(Long studentId);

    // 교수 스케쥴로 확정된 아이디 조회
    List<CounselingReserve> findByCounselingSchedule_IdIn(List<Long> ids);

}
