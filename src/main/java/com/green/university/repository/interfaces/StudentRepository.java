package com.green.university.repository.interfaces;

import com.green.university.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student,Long>,
		JpaSpecificationExecutor<Student> {

	// 전체 학생의 id만 가져오기
	@Query ("SELECT s.id FROM Student s")
	List<Long> findAllStudentIds();

	// id 찾기
    Long findByNameAndEmail(String name, String email);

	// password 찾기
    Long findByIdAndNameAndEmail(Long id, String name, String email);

	// =============== 페이징 하는 방법 1. @Query 쓰기 2. SpecificationExecutor 만들기
	@Query("SELECT s FROM Student s WHERE " +
			"(:studentId IS NULL OR s.id = :studentId) AND " +
			"(:deptId IS NULL OR s.department.id = :deptId)")
	Page<Student> findByOptionalStudentIdAndDeptId(
			@Param("studentId") Long studentId,
			@Param("deptId") Long deptId,
			Pageable pageable
	);

	// =============== JPQL로 하거나 @Query 쓰거나 ..
	// 학생 grade, semester 업데이트
	@Query("SELECT s.id, COUNT(t.tuiYear) FROM Student s " +
			"JOIN Tuition t ON s.id = t.student.id " +
			"GROUP BY s.id")
	List<Object[]> findStudentTuitionCounts();
	int updateGradeAndSemesterById(Long studentId, int grade, int semester);

	/**
	@Modifying(clearAutomatically = true)
	@Query("""
    UPDATE Student s
    SET s.grade = :grade,
        s.semester = :semester 
    WHERE s.id IN (
        SELECT t.student.id FROM Tuition t 
        GROUP BY t.student.id 
        HAVING COUNT(t.tuiYear) = :count
    )
    """)
	int updateByTuitionCount(@Param("grade") int grade,
							 @Param("semester") int semester,
							 @Param("count") int count);
	*/


}
