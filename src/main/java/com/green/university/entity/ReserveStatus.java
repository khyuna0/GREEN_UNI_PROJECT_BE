package com.green.university.entity;

public enum ReserveStatus { // 예약 폼 스테이터스
    REQUESTED,   // 학생 신청
    APPROVED,    // 교수 승인 → 예약 생성
    REJECTED     // 반려
}
