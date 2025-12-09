package com.green.university.repository.interfaces;

import com.green.university.dto.UpdateStudentGradeDto;

import com.green.university.entity.StuSub;
import com.green.university.entity.StuSubDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// stu_sub_detail_tb DAO
public interface StuSubDetailRepository extends JpaRepository<StuSubDetail,Long> {

	Optional<StuSubDetail> findByStuSub(StuSub stuSub);

    // 과목으로 학생 상세정보 뽑기 (StudentInfoForProfessorDto)
    List<StuSubDetail> findBySubject_Id(Long subjectId);
}
