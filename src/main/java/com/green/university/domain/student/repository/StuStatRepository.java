package com.green.university.domain.student.repository;

import com.green.university.domain.student.dto.StuStat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StuStatRepository extends JpaRepository<StuStat,Long> {

	// 해당 학생의 모든 학적 변동 내역 조회
	List<StuStat> findAllByStudentIdOrderByIdDesc(Long studentId);

	// 학생 내정보 조회에 학적변동리스트 (최신순으로 정렬함 - 내림차순)
	List<StuStat> findByStudent_IdOrderByIdDesc(Long studentId);
}
