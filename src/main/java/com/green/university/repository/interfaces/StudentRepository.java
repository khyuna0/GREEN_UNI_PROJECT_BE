package com.green.university.repository.interfaces;

import com.green.university.dto.*;
import com.green.university.dto.response.StudentInfoDto;
import com.green.university.dto.response.UserInfoForUpdateDto;
import com.green.university.repository.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

/**
 * Student DAO
 * 
 * @author 김지현
 */

public interface StudentRepository extends JpaRepository<Student,Long> {

	// student_tb에 insert
	public Long insertToStudent(CreateStudentDto createStudentDto);

	// staff_tb에서 자동 생성된 id 받아오기
	public Long selectIdByCreateStudentDto(CreateStudentDto createStudentDto);

	/**
	 * @author 서영 전체 학생의 id만 가져오기
	 */
	public List<Long> selectIdList();

	/**
	 * @author 서영 특정 학생의 정보 가져오기
	 */
	public Student selectByStudentId(Long studentId);

	// 업데이트용 정보 읽기
	public UserInfoForUpdateDto selectByUserId(Long userId);

	// 유저 정보 업데이트
	public Long updateStudent(UserUpdateDto userUpdateDto);

	// 학생 info id로 불러오기
	public StudentInfoDto selectStudentInfoById(Long id);

	// id 찾기
	public Long selectIdByNameAndEmail(FindIdFormDto findIdFormDto);

	// password 발급용 model 확인
	public Long selectStudentByIdAndNameAndEmail(FindPasswordFormDto findPasswordFormDto);
	
	// 페이지별 학생 조회
	public List<Student> selectStudentList(StudentListForm studentListForm);
	
	// 페이지, 과별 학생조회
	public List<Student> selectByDepartmentId(StudentListForm studentListForm);
	
	// 학번으로 학생 조회
	public List<Student> selectByStudentId(StudentListForm studentListForm);
	
	// 페이징 처리 위한 전체 학생 수 조회
	public Long selectStudentAmount();
	
	// 페이징 처리 위한 과 학생 수 조회
	public Long selectStudentAmountByDeptId(Long deptId);
	
	// 학생 grade, semester 업데이트
	public Long updateStudentGradeAndSemester1_2();
	public Long updateStudentGradeAndSemester2_1();
	public Long updateStudentGradeAndSemester2_2();
	public Long updateStudentGradeAndSemester3_1();
	public Long updateStudentGradeAndSemester3_2();
	public Long updateStudentGradeAndSemester4_1();
	public Long updateStudentGradeAndSemester4_2();
	
	
	
}
