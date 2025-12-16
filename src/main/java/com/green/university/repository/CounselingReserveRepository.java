package com.green.university.repository;

import com.green.university.dto.response.CounselingReserveDto;
import com.green.university.entity.CounselingReserve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounselingReserveRepository extends JpaRepository<CounselingReserve , Long> {

    List<CounselingReserveDto> findByStudentId(Long studentId);

}
