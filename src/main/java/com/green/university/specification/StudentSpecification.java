package com.green.university.specification;

import com.green.university.entity.Student;
import org.springframework.data.jpa.domain.Specification;

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