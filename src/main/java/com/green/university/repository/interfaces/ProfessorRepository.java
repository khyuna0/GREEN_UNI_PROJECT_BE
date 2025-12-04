package com.green.university.repository.interfaces;

import com.green.university.entity.Professor;
import com.green.university.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    // id 찾기
    Professor findByNameAndEmail(String name, String email);

    // password 발급용 엔티티? 확인
    Staff findByIdAndNameAndEmail(Long id, String name, String email);

    // 페이지, 과별 교수 조회 (검색 용)
    Page<Professor> findByDepartment_id(Long departmentId, Pageable pageable);

    // 페이징 처리 위한 과 교수 수 조회 (컨트롤러 수정할 때 삭제)
    Long countByDepartment_id(Long departmentId);


}
