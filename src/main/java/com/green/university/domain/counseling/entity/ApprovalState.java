package com.green.university.domain.counseling.entity;

public enum ApprovalState { // 예약 폼 스테이터스
    REQUESTED,   // 상담 신청(학생 , 교수 둘다)
    APPROVED,    // 승인 → 예약 생성
    REJECTED    // 반려
}
