package com.green.university.domain.student.service;

import com.green.university.domain.student.dto.StudentDto;
import com.green.university.domain.university.repository.DepartmentRepository;
import com.green.university.domain.student.repository.StudentRepository;
import com.green.university.domain.student.entity.Student;
import com.green.university.domain.student.specification.StudentSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 학생 관련 서비스
 *
 * @author 김지현
 */
@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * @return 학생 리스트
     */
    @Transactional(readOnly = true)
    public Page<StudentDto> readStudentList(Long studentId, Long deptId, Pageable pageable) {

        Specification<Student> spec = (
                root, query, cb) -> null; // 조건 없이 전체 조회
        if (studentId != null) {
            spec = spec.and(StudentSpecification.hasStudentId(studentId));
        }
        if (deptId != null) {
            spec = spec.and(StudentSpecification.hasDepartment(deptId));
        }
        if (studentId != null && deptId != null) {
            spec = spec.and(StudentSpecification.hasStudentIdAndDepartment(studentId, deptId));
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
        for (Object[] row : results) {
            Long studentId = (Long) row[0];
            Long count = (Long) row[1]; // 등록횟수

            Optional<Student> studentOpt = studentRepository.findById(studentId);
            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();

                // 2️학년/학기 계산
                Long grade = (long) Math.min(4, (int) ((count + 1) / 2));
                Long semester = (long) ((count % 2 == 0) ? 2 : 1);

                // 3️setter로 직접 수정
                student.setGrade(grade);
                student.setSemester(semester);

                // 4️save (자동 UPDATE)
                studentRepository.save(student);
                totalUpdated++;
            }
        }
        return totalUpdated;

    }
}
