package com.green.university.domain.dropoutrisk.entity;

// 위험 유형
public enum RiskType {
    ATTENDANCE, // 출석 위험
    SUBJECT_GRADE,     // 과목 성적(F/D 등) - 과목 단위
    BOTH,      // 출결 + 성적 둘 다 위험
    SEMESTER_GPA       // 학기 누계(평점) - subject = null
}
