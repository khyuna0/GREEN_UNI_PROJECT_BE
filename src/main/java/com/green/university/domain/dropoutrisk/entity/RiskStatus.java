package com.green.university.domain.dropoutrisk.entity;

// 처리 상태
public enum RiskStatus {
    DETECTED,    // 감지됨 (학생/교수 알림 띄움)
    CONSULT_REQ, // 상담 예약됨
    RESOLVED     // 상담 완료, 해결됨
}
