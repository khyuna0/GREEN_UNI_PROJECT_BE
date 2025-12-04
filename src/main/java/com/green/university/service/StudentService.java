package com.green.university.service;

import com.green.university.dto.StudentListForm;
import com.green.university.dto.response.StudentDto;
import com.green.university.repository.interfaces.DepartmentRepository;
import com.green.university.repository.interfaces.StudentRepository;
import com.green.university.entity.Student;
import com.green.university.repository.specification.StudentSpecification;
import com.green.university.utils.Define;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
	public Page<StudentDto> readStudentList(StudentListForm studentListForm) {
		// 1. 페이징 관련된 정보
		int page = studentListForm.getPage() == null ? 0 : studentListForm.getPage().intValue();
		if (page < 0) page = 0;

		Pageable pageable = PageRequest.of(page, Define.STUDENT_PAGE_SIZE);

		Long studentId = studentListForm.getStudentId();
		Long deptId = studentListForm.getDeptId();

		// 2. Specification 사용하기
		Specification<Student> spec = (
				root, query, cb) -> null; // 조건 없이 전체 조회
		if (studentId != null) {
			spec = spec.and(StudentSpecification.hasStudentId(studentId));
		}
		if (deptId != null) {
			spec = spec.and(StudentSpecification.hasDepartment(deptId));
		}

		Page<Student> Student = studentRepository.findAll(spec, pageable);
		return Student.map(StudentDto::fromEntity); // dto로 반환해주기

//		2. @Query문 사용하기
//		Page<Student> result = studentRepository.findByOptionalStudentIdAndDeptId(
//				studentListForm.getStudentId(), studentListForm.getDeptId(), pageable);
//
//		return result.map(StudentDto::fromEntity);
	}

	// tuition_tb 등록 횟수로 학년/학기 업데이트
	@Transactional
	public int updateStudentGradeAndSemester() {
		List<Object[]> results = studentRepository.findStudentTuitionCounts();
		// results = [[학생1, 3], [학생2, 5], [학생3, 8]]

		int totalUpdated = 0;
		for(Object[] row : results) {
			Long studentId = (Long)row[0];
			Long count = (Long)row[1]; // 등록횟수
			int grade = Math.min(4, (int)((count+1)/2));
			int semester = (count % 2 == 0) ? 2 : 1;
			totalUpdated += studentRepository.updateGradeAndSemesterById(studentId, grade, semester);
		}
		return totalUpdated;
	}


}
