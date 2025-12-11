package com.green.university.utils;

import com.green.university.dto.PenaltyResultDto;

public class PenaltyCalculator {

    public static PenaltyResultDto calculate(long absent, long lateness) {

        long latenessToAbsent = lateness / 3;  // 지각 3 → 결석 1
        long totalAbsent = absent + latenessToAbsent;

        double penalty = totalAbsent * 2;  // 결석 1 → -2점

        return new PenaltyResultDto(totalAbsent, penalty);
    }
}
