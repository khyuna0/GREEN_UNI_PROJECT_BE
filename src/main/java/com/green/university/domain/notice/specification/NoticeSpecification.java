package com.green.university.domain.notice.specification;

import com.green.university.domain.notice.entity.Notice;
import org.springframework.data.jpa.domain.Specification;

public class NoticeSpecification {

    // 제목에 keyword 포함
    public static Specification<Notice> titleContains(String keyword) {
        return (root, query, cb) ->
                keyword == null || keyword.isBlank()
                        ? null
                        : cb.like(
                        cb.lower(root.get("title")),
                        "%" + keyword.trim().toLowerCase() + "%"
                );
    }

    // 내용에 keyword 포함
    public static Specification<Notice> contentContains(String keyword) {
        return (root, query, cb) ->
                keyword == null || keyword.isBlank()
                        ? null
                        : cb.like(
                        cb.lower(root.get("content")),
                        "%" + keyword.trim().toLowerCase() + "%"
                );
    }

    // 제목 OR 내용에 keyword 포함
    public static Specification<Notice> titleOrContentContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;

            String like = "%" + keyword.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("content")), like)
            );
        };
    }
}
