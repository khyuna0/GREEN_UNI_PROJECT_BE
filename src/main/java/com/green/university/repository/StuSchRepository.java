package com.green.university.repository;

import com.green.university.entity.StuSch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StuSchRepository extends JpaRepository <StuSch, Long> {

    // 학생 아이디로 해당 학생이 어떤 년도, 학기에 장학금 유형인지 찾기
    StuSch findByStudent_IdAndSchYearAndSemester(
            Long studentId,
            Long schYear,
            Long semester
    );
}
