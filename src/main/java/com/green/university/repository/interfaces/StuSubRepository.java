package com.green.university.repository.interfaces;

import com.green.university.entity.StuSub;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StuSubRepository extends JpaRepository<StuSub, Long> {

    // 과목으로 학생 상세정보 뽑기 (StudentInfoForProfessorDto)
    List<StuSub> findBySubject_Id(Long subjectId);

    // 학생의 수강 신청 내역에 해당 강의가 있는지 조회 (stu_sub_tb의 grade 컬럼에 성적 입력)
    Optional<StuSub> findByStudent_IdAndSubject_Id(Long studentId, Long subjectId);

    // 학생의 이번 학기 전체 수강 내역 조회
    List<StuSub> findByStudent_IdAndSubYearAndSemester(Long studentId, Long subYear, Long semester);

    // 수강 신청 내역 삭제
    void deleteByStudent_IdAndSubject_Id(Long studentId, Long subjectId);

    // 수강 신청 내역과 예비 수강 신청 내역 조인 후 조회
    // type == 1 : 수강 신청, 예비 수강 신청에 둘 다 존재
    // type == 0 : 예비 수강 신청에만 존재
    List<StuSub> findByStudent_Id(Long studentId);

}
