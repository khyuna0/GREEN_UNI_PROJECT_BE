package com.green.university.domain.evaluation.specification;

import com.green.university.domain.evaluation.entity.Evaluation;
import com.green.university.domain.professor.entity.Professor;
import com.green.university.domain.subject.entity.Subject;
import jakarta.persistence.criteria.Join;
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
