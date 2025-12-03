package com.green.university.repository.interfaces;

import com.green.university.entity.Department;
import com.green.university.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

	// id 찾기 - FindIdFormDto
    Long findByNameAndEmail(String name, String email);

	// password 발급용 model 확인 - FindPasswordFormDto
    Long findByIdAndNameAndEmail(Long id, String name, String email);

	// =============== 아래 코드는 페이징 하는 것 같은데 .. 추후 수정 ..

	@Query("SELECT s FROM Student s WHERE " +
			"(:studentId IS NULL OR s.id = :studentId) AND " +
			"(:deptId IS NULL OR s.department.id = :deptId)")
	Page<Student> findByOptionalStudentIdAndDeptId(
			@Param("studentId") Long studentId,
			@Param("deptId") Long deptId,
			Pageable pageable
	);

	// 페이지별(?) 학생 조회 - StudentListForm
	// 기존 코드는 폼으로도 찾고, 과별로 찾고, 학번으로 찾았었음 ..
	//Page<Student> findById(Long id, Pageable pageable);
	Page<Student> findByDepartment(Department department, Pageable pageable);
    Page<Student> findByIdAndDepartment(Long id, Department department, Pageable pageable);
	Page<Student> findAll(Pageable pageable);

	// 페이징 처리 위한 전체 학생 수 조회
    Long selectStudentAmount();
	// 페이징 처리 위한 과 학생 수 조회
    Long selectStudentAmountByDeptId(Long deptId);

	// =============== 이건 리액트에서 랜더링 하면 되는 거 아닌가 ..?

	// 학생 grade, semester 업데이트
    Long updateStudentGradeAndSemester1_2();
	Long updateStudentGradeAndSemester2_1();
	Long updateStudentGradeAndSemester2_2();
	Long updateStudentGradeAndSemester3_1();
	Long updateStudentGradeAndSemester3_2();
	Long updateStudentGradeAndSemester4_1();
	Long updateStudentGradeAndSemester4_2();


	// 데이터베이스에서 데이터를 조회할 때 동적인 쿼리를 작성할 수 있는 jpa에서 제공하는 인터페이스
	public class StudentSpecification {

		// ID로 찾기
		public static Specification<Student> hasStudentId(Long studentId) {
			return (root, query, cb) ->
					studentId == null ? null : cb.equal(root.get("id"), studentId);
		}

		// 학과로 찾기
		public static Specification<Student> hasDepartment(Long deptId) {
			return (root, query, cb) ->
					deptId == null ? null :
							cb.equal(root.get("department").get("id"), deptId);
		}

		// 두 조건 조합 (AND)
		public static Specification<Student> hasStudentIdAndDepartment(Long studentId, Long deptId) {
			return Specification.where(hasStudentId(studentId))
					.and(hasDepartment(deptId));
		}
	}


}
