package com.green.university.repository.interfaces;

import com.green.university.entity.PreStuSub;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PreStuSubRepository extends JpaRepository<PreStuSub, Long> {

    // 학생의 예비 수강 신청 내역에 해당 강의가 있는지 조회
    PreStuSub findByStudent_IdAndSubject_Id(Long studentId, Long subjectId);

    // 학생의 예비 수강 신청의 총 학점 구하기 용도
    List<PreStuSub> findByStudent_Id(Long studentId);

    // 예비 수강 신청 내역에 해당 강의가 있는 학생들 조회
    List<PreStuSub> findBySubject_Id(Long subjectId);

}
