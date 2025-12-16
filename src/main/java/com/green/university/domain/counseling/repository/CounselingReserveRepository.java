package com.green.university.domain.counseling.repository;

import com.green.university.domain.counseling.dto.CounselingReserveDto;
import com.green.university.domain.counseling.entity.CounselingReserve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounselingReserveRepository extends JpaRepository<CounselingReserve, Long> {

    List<CounselingReserveDto> findByStudentId(Long studentId);

}
