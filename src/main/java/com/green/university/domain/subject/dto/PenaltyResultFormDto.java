package com.green.university.domain.subject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PenaltyResultFormDto {
    private long totalAbsent;
    private double penalty;
}
