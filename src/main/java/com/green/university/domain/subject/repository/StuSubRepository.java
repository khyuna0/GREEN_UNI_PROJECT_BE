package com.green.university.domain.subject.repository;

import com.green.university.domain.subject.entity.StuSub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StuSubRepository extends JpaRepository<StuSub, Long> {


    // 학생의 수강 신청 내역에 해당 강의가 있는지 조회 (stu_sub_tb의 grade 컬럼에 성적 입력)
    Optional<StuSub> findByStudent_IdAndSubject_Id(Long studentId, Long subjectId);

    // 학생의 특정 연도 + 학기 전체 수강 내역 조회
    List<StuSub> findByStudent_IdAndSubject_SubYearAndSubject_Semester(Long studentId, Long subYear, Long semester);

    // 수강 신청 내역과 예비 수강 신청 내역 조인 후 조회
    // type == 1 : 수강 신청, 예비 수강 신청에 둘 다 존재
    // type == 0 : 예비 수강 신청에만 존재
    List<StuSub> findByStudent_Id(Long studentId);

    // ==========================================
    // 학생의 전체 수강/성적 내역 (연도/학기 최신순)
    List<StuSub> findByStudent_IdOrderBySubject_SubYearDescSubject_SemesterDesc(Long studentId);

    // 특정 연도 + 학기 + 과목 타입(전공/교양 등)
    List<StuSub> findByStudent_IdAndSubject_SubYearAndSubject_SemesterAndSubject_Type(
            Long studentId,
            Long subYear,
            Long semester,
            String type
    );

    // 학생 ID와 과목 ID로 수강신청 내역 존재 여부 확인
    boolean existsByStudentIdAndSubjectId(Long studentId, Long subjectId);

    // 학생 아이디로 검색한 stuSub 중 년도와 학기 특정된 subject를 가진 stuSub 목록 가져오기
    @Query("""
    select ss
    from StuSub ss
    join StuSubDetail sd on sd.stuSub.id = ss.id
    where ss.student.id = :studentId
      and ss.subject.subYear = :year
      and ss.subject.semester = :semester
      and sd.finalized = true
""")
    List<StuSub> findByStudentAndTermAndFinalized(
            @Param("studentId") Long studentId,
            @Param("year") Long year,
            @Param("semester") Long semester
    );

}
