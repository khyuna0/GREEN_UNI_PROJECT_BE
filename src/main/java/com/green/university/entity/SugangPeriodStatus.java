package com.green.university.entity;

import lombok.Getter;

@Getter
public enum SugangPeriodStatus {
    PRELIMINARY(0, "예비 수강신청 기간"),
    REGISTRATION(1, "수강신청 기간"),
    CLOSED(2, "수강신청 종료");

    private final int code;
    private final String description;

    SugangPeriodStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SugangPeriodStatus fromCode(int code) {
        for (SugangPeriodStatus status : values()) {
            if (status.code == code) return status;
        }
        throw new IllegalArgumentException("잘못된 코드: " + code);
    }
}
