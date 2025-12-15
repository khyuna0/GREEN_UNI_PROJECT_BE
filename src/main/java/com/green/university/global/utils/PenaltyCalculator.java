package com.green.university.global.utils;

import com.green.university.domain.subject.dto.PenaltyResultFormDto;

public class PenaltyCalculator {

    public static PenaltyResultFormDto calculate(long absent, long lateness) {

        long latenessToAbsent = lateness / 3;  // 지각 3 → 결석 1
        long totalAbsent = absent + latenessToAbsent;

        double penalty = totalAbsent * 2;  // 결석 1 → -2점

        return new PenaltyResultFormDto(totalAbsent, penalty);
    }
}
