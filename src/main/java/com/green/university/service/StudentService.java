package com.green.university.service;

import com.green.university.dto.StudentListForm;
import com.green.university.entity.Department;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.repository.interfaces.DepartmentRepository;
import com.green.university.repository.interfaces.StudentRepository;
import com.green.university.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 학생 관련 서비스
 * 
 * @author 김지현
 *
 */
@Service
public class StudentService {

	@Autowired
	private StudentRepository studentRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

	/**
	 * 
	 * @param studentListForm
	 * @return 학생 리스트
	 */
	@Transactional
	public List<Student> readStudentList(StudentListForm studentListForm) {
		Long studentId = studentListForm.getStudentId();
		Long deptId = studentListForm.getDeptId();

		// id가 있으면 그걸로 조회, dept가 있으면 그걸로 조회 -> 둘 다 있으면 둘 다로 조회..
		if (studentId != null && deptId != null) {
			Department department = departmentRepository.findById(deptId)
					.orElseThrow(() -> new CustomRestfullException("Department not found", HttpStatus.BAD_REQUEST));
			return studentRepository.findByIdAndDepartment(studentId, department);
		} else if (studentId != null) {
			return studentRepository.findById(studentId)
					.map(List::of)
					.orElse(List.of());
		} else if (deptId != null) {
			Department department = departmentRepository.findById(deptId)
					.orElseThrow(() -> new CustomRestfullException("Department not found", HttpStatus.BAD_REQUEST));
			return studentRepository.findByDepartment(department);
		} else {
			// 조건이 하나도 없으면 전체 조회 or 빈 리스트 반환 가능
			return List.of();
		}
	}

	/**
	 * 
	 * @param studentListForm
	 * @return 학생 수
	 */
	@Transactional
	public Long readStudentAmount(StudentListForm studentListForm) {

		Long amount = null;
		if (studentListForm.getDeptId() != null) {
			amount = studentRepository.selectStudentAmountByDeptId(studentListForm.getDeptId());
		} else {
			amount = studentRepository.selectStudentAmount();
		}

		return amount;
	}

	/**
	 * 학생 학년과 학기 업데이트
	 */
	@Transactional
	public void updateStudentGradeAndSemester() {
		studentRepository.updateStudentGradeAndSemester1_2();
		studentRepository.updateStudentGradeAndSemester2_1();
		studentRepository.updateStudentGradeAndSemester2_2();
		studentRepository.updateStudentGradeAndSemester3_1();
		studentRepository.updateStudentGradeAndSemester3_2();
		studentRepository.updateStudentGradeAndSemester4_1();
		studentRepository.updateStudentGradeAndSemester4_2();
	}

}
