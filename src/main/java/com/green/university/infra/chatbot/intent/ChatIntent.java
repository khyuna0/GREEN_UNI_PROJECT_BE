package com.green.university.infra.chatbot.intent;

public enum ChatIntent {

    // 학생 - 성적
    GRADE_CURRENT,
    GRADE_SEMESTER,
    GRADE_TOTAL,

    // 공지/학사일정
    NOTICE_LIST,
    SCHEDULE_LIST,

    // 수업/수강신청
    SUBJECT_LIST,
    SUGANG_LIST,
    SUGANG_PRE,
    SUGANG_APPLY,
    SUGANG_TIMETABLE,
    SUGANG_PERIOD, // (직원) 수강신청 기간 설정

    // 사용자
    USER_INFO,
    USER_PW,

    // 휴학
    BREAK_APP,
    BREAK_LIST_STUDENT,
    BREAK_LIST_STAFF,

    // 등록금
    TUITION_LIST,
    TUITION_PAYMENT,
    TUITION_BILL_CREATE, // (직원) 고지서 생성

    // 명단 조회
    PROFESSOR_LIST,
    STUDENT_LIST,

    // 교수
    PROFESSOR_SUBJECT,
    PROFESSOR_EVALUATION,

    // 관리자(등록)
    ADMIN_COLLEGE,
    ADMIN_DEPARTMENT,
    ADMIN_ROOM,
    ADMIN_SUBJECT,
    ADMIN_COLLTUIT,

    // 범위 밖
    OUT_OF_SCOPE,
    UNKNOWN
}
