package com.green.university.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PenaltyResultDto {
    private long totalAbsent;
    private int penalty;
}
