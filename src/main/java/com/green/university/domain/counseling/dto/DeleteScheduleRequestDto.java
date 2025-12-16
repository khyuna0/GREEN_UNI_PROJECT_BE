package com.green.university.domain.counseling.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DeleteScheduleRequestDto {
    private LocalDate counselingDate;
    private Long startTime;
}