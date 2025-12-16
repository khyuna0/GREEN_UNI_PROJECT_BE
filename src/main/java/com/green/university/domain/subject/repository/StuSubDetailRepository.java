package com.green.university.domain.subject.repository;

import com.green.university.domain.subject.entity.StuSub;
import com.green.university.domain.subject.entity.StuSubDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// stu_sub_detail_tb DAO
public interface StuSubDetailRepository extends JpaRepository<StuSubDetail,Long> {

	Optional<StuSubDetail> findByStuSub(StuSub stuSub);

    // 과목으로 학생 상세정보 뽑기 (StudentInfoForProfessorDto)
    List<StuSubDetail> findBySubject_Id(Long subjectId);

    // 과목으로 학생 상세정보 뽑아 환산점수 높은 순 정렬
    List<StuSubDetail> findBySubject_IdOrderByConvertedMarkDesc(Long subjectId);

    List<StuSubDetail> findBySubject_IdAndFinalizedTrue(Long subjectId);
}
