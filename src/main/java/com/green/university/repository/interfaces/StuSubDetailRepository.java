package com.green.university.repository.interfaces;

import com.green.university.dto.UpdateStudentGradeDto;

import com.green.university.entity.StuSub;
import com.green.university.entity.StuSubDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// stu_sub_detail_tb DAO
public interface StuSubDetailRepository extends JpaRepository<StuSubDetail,Long> {

	Optional<StuSubDetail> findByStuSub(StuSub stuSub);
}
