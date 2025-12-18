package com.green.university.domain.counseling.entity;

public enum ReserveRequester {
    STUDENT,  // 학생이 신청 -> 교수가 승인/반려
    PROFESSOR;  // 교수가 신청 -> 학생이 승인/반려
}
