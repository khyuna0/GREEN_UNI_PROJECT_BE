package com.green.university.repository;

import com.green.university.entity.StuSub;
import com.green.university.entity.StuSubDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// stu_sub_detail_tb DAO
public interface StuSubDetailRepository extends JpaRepository<StuSubDetail,Long> {

	Optional<StuSubDetail> findByStuSub(StuSub stuSub);
}
