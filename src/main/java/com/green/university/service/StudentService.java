package com.green.university.service;

import com.green.university.dto.StudentListForm;
import com.green.university.dto.response.StudentDto;
import com.green.university.dto.response.StudentInfoDto;
import com.green.university.entity.Department;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.repository.interfaces.DepartmentRepository;
import com.green.university.repository.interfaces.StudentRepository;
import com.green.university.entity.Student;
import com.green.university.utils.Define;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
	@Transactional (readOnly = true)
	public Page<StudentDto> readStudentList(StudentListForm studentListForm, Integer page) {
		Pageable pageable = PageRequest.of(page, Define.STUDENT_PAGE_SIZE);

		// 1. Specification 사용하기
		Long studentId = studentListForm.getStudentId();
		Long deptId = studentListForm.getDeptId();

		Specification<Student> spec = Specification.where(null); // 조건 없이 전체 조회
		if (studentId != null && deptId != null) {
			spec = spec.and(StudentRepository.StudentSpecification.hasStudentId(studentId))
					.and(StudentRepository.StudentSpecification.hasDepartment(deptId));
		} else if (studentId != null) {
			spec = spec.and(StudentRepository.StudentSpecification.hasStudentId(studentId));
		} else if (deptId != null) {
			spec = spec.and(StudentRepository.StudentSpecification.hasDepartment(deptId));
		}

		Page<Student> studentPage = studentRepository.findAll(spec, pageable);
		return studentPage.map(StudentDto::fromEntity);

//		2. @Query문 사용하기
//		Page<Student> result = studentRepository.findByOptionalStudentIdAndDeptId(
//				studentListForm.getStudentId(), studentListForm.getDeptId(), pageable);
//
//		return result.map(StudentDto::fromEntity);
	}

	/**
	 * 
	 * @param studentListForm
	 * @return 학생 수
	 */
	@Transactional (readOnly = true)
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
