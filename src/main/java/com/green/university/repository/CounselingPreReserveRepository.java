package com.green.university.repository;

import com.green.university.dto.response.CounselingPreReserveDto;
import com.green.university.entity.CounselingPreReserve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounselingPreReserveRepository extends JpaRepository<CounselingPreReserve, Long> {

    List<CounselingPreReserve> findByStudentId(Long studentId);

    List<CounselingPreReserve> findBySubjectId(Long subjectId);

    List<CounselingPreReserve>
    findBySubject_IdIn(List<Long> subjectIds);

    List<CounselingPreReserve>
    findByCounselingSchedule_Id(Long scheduleId);

}
