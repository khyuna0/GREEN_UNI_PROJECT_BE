package com.green.university.repository.specification;

import com.green.university.entity.Evaluation;
import com.green.university.entity.Professor;
import com.green.university.entity.Subject;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class EvaluationSpecification {
    // professorId 조건 — 고정
    public static Specification<Evaluation> professorIdEq(Long professorId) {
        return (root, query, cb) -> {
            Join<Evaluation, Subject> subject = root.join("subject");
            Join<Subject, Professor> professor = subject.join("professor");
            return cb.equal(professor.get("id"), professorId);
        };
    }

    // name 조건 — 선택
    public static Specification<Evaluation> subjectNameEq(String name) {
        return (root, query, cb) -> {
            Join<Evaluation, Subject> subject = root.join("subject");
            return cb.equal(subject.get("name"), name);
        };
    }
}
