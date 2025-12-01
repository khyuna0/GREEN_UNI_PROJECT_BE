package com.green.university.repository.interfaces;

import com.green.university.dto.*;
import com.green.university.entity.Department;
import com.green.university.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

/**
 * Student DAO
 * 
 * @author 김지현
 */

public interface StudentRepository extends JpaRepository<Student,Long> {

	/**
	 * @author 서영 전체 학생의 id만 가져오기
	 */
    List<Long> findStudentIds();

	// id 찾기 - FindIdFormDto
    Long findByNameAndEmail(String name, String email);

	// password 발급용 model 확인 - FindPasswordFormDto
    Long findByIdAndNameAndEmail(Long id, String name, String email);


	// 페이지별 학생 조회
    List<Student> findByIdAndDepartment(Long id, Department department);
	List<Student> findByDepartment(Department department);

	//**********************************
	// 페이지, 과별 학생조회
    List<Student> selectByDepartmentId(StudentListForm studentListForm);
	
	// 학번으로 학생 조회
    List<Student> selectByStudentId(StudentListForm studentListForm);
	
	// 페이징 처리 위한 전체 학생 수 조회
    Long selectStudentAmount();
	
	// 페이징 처리 위한 과 학생 수 조회
    Long selectStudentAmountByDeptId(Long deptId);
	
	// 학생 grade, semester 업데이트
    Long updateStudentGradeAndSemester1_2();
	Long updateStudentGradeAndSemester2_1();
	Long updateStudentGradeAndSemester2_2();
	Long updateStudentGradeAndSemester3_1();
	Long updateStudentGradeAndSemester3_2();
	Long updateStudentGradeAndSemester4_1();
	Long updateStudentGradeAndSemester4_2();



}
