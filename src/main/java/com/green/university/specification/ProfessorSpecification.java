package com.green.university.specification;

import com.green.university.entity.Professor;
import org.springframework.data.jpa.domain.Specification;

import javax.management.Query;

public class ProfessorSpecification {

    // ID 조회
    public static Specification<Professor> hasProfessorId(Long professorId) {
        return (root, query, cb) ->
                professorId == null ? null : cb.equal(root.get("id"), professorId);
    }

    public static Specification<Professor> hasDepartmentName(String deptName) {
        return (root, query, cb) -> deptName == null ? null :
                cb.equal(root.get("department").get("name"), deptName);
    }

    public static Specification<Professor> hasProfessorIdAndDepartmentName(Long professorid, String deptName) {
        return Specification.where(hasProfessorId(professorid)).and(hasDepartmentName(deptName));
    }
}
