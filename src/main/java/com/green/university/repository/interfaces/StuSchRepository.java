package com.green.university.repository.interfaces;

import com.green.university.entity.Scholarship;
import com.green.university.entity.StuSch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StuSchRepository extends JpaRepository <StuSch, Long> {

    Scholarship findSchTypeByStudentIdAndSchYearAndSemester(
            Long studentId,
            Long schYear,
            Long semester
    );
}
