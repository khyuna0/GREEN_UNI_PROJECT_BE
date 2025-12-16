package com.green.university.domain.counseling.repository;

import com.green.university.domain.counseling.entity.CounselingPreReserve;
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
