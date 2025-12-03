package com.green.university.repository.interfaces;

import com.green.university.entity.Grade;
import com.green.university.entity.StuSub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRepository extends JpaRepository<StuSub, Long> {

    // 학생의 전체 수강/성적 내역 (연도/학기 최신순)
    List<StuSub> findByStudent_IdOrderBySubject_SubYearDescSubject_SemesterDesc(Long studentId);

    // 학생의 전체 수강/성적 내역
    List<StuSub> findByStudent_Id(Long studentId); // 위에랑 이거중에 둘중 하나써도 될듯?

    // 특정 연도 + 학기 (전체)
    List<StuSub> findByStudent_IdAndSubject_SubYearAndSubject_Semester(
            Long studentId,
            Long subYear,
            Long semester
    );

    // 특정 연도 + 학기 + 과목 타입(전공/교양 등)
    List<StuSub> findByStudent_IdAndSubject_SubYearAndSubject_SemesterAndSubject_Type(
            Long studentId,
            Long subYear,
            Long semester,
            String type
    );

    Grade findByGradeName(String grade);
}
