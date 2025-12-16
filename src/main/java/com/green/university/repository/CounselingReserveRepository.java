package com.green.university.repository;

import com.green.university.dto.response.CounselingReserveDto;
import com.green.university.entity.CounselingReserve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounselingReserveRepository extends JpaRepository<CounselingReserve , Long> {

    List<CounselingReserve> findByStudentId(Long studentId);
    
    // 교수 스케쥴로 확정된 아이디 조회
    List<CounselingReserve> findByCounselingSchedule_IdIn(List<Long> ids);

}
