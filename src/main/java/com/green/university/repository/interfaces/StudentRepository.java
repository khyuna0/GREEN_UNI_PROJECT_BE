package com.green.university.repository.interfaces;

import com.green.university.entity.Department;
import com.green.university.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;

/**
 * Student DAO
 * 
 * @author 김지현
 */

public interface StudentRepository extends JpaRepository<Student,Long> {

	/**
	 * @author 서영
	 * 전체 학생의 id만 가져오기
	 */
	@Query ("SELECT s.id FROM Student s")
	List<Long> findAllStudentIds();

	// id 찾기 - FindIdFormDto
    Long findByNameAndEmail(String name, String email);

	// password 발급용 model 확인 - FindPasswordFormDto
    Long findByIdAndNameAndEmail(Long id, String name, String email);

	// =============== 아래 코드는 페이징 하는 것 같은데 .. 추후 수정 ..

	// 페이지별(?) 학생 조회 - StudentListForm
	// 기존 코드는 폼으로도 찾고, 과별로 찾고, 학번으로 찾았었음 ..
    List<Student> findByIdAndDepartment(Long id, Department department);
	List<Student> findByDepartment(Department department);
	
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



}
