package com.green.university.domain.subject.specification;

import com.green.university.domain.subject.entity.Subject;
import com.green.university.domain.university.entity.Department;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

// 데이터베이스에서 데이터를 조회할 때 동적인 쿼리를 작성할 수 있는 jpa에서 제공하는 인터페이스
public class SubjectSpecification {

    // 현재 연도, 학기로 찾기
    public static Specification<Subject> currentSemester(Long subYear, Long semester) {
        return (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("subYear"), subYear),
                        cb.equal(root.get("semester"), semester)
                );
    }

    // 전공 또는 교양인지 찾기
    public static Specification<Subject> hasType(String type) {
        return (root, query, cb) ->
                type == null || type.isBlank()
                        ? null
                        : cb.equal(root.get("type"), type);
    }

    // 학과 id로 찾기
    public static Specification<Subject> hasDepartmentId(Long deptId) {
        return (root, query, cb) ->
                deptId == null
                        ? null
                        : cb.equal(root.get("department").get("id"), deptId);
    }

    // 학과명으로 찾기 (Subject 엔티티에 department_name이 없어서 join 해서 찾아야 함)
    public static Specification<Subject> hasDepartmentName(String deptName) {
        return (root, query, cb) -> {
            if (deptName == null || deptName.isBlank()) {
                return null;
            }
            // Subject -> Department JOIN
            Join<Subject, Department> deptJoin = root.join("department");
            return cb.like(deptJoin.get("name"), "%" + deptName + "%");
        };
    }

    // 강의명으로 찾기
    public static Specification<Subject> nameContains(String name) {
        return (root, query, cb) ->
                name == null || name.isBlank()
                        ? null
                        : cb.like(root.get("name"), "%" + name + "%");
    }
}
