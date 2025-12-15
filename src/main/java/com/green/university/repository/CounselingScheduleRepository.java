package com.green.university.repository;

import com.green.university.entity.CounselingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounselingScheduleRepository extends JpaRepository<CounselingSchedule, Long> {

    List<CounselingSchedule> findByProfessorId(Long professorId); // 내가 열어 둔 상담 일정 보기

}
