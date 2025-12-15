package com.green.university.domain.breakapp.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;


/**
 * @author 서영
 *
 */

@Data
public class BreakAppFormDto {

    private Long studentId;
    private Long studentGrade;
//    @NotNull    (message = "시작 년도를 입력해주세요")
    private Long fromYear;
//    @NotNull    (message = "시작 학기를 입력해주세요")
    private Long fromSemester;
    @NotNull (message = "종료 년도를 입력해주세요")
    private Long toYear;
    @NotNull    (message = "종료 학기를 입력해주세요")
    private Long toSemester;
    @NotNull(message = "휴학 사유를 입력해주세요")
    private String type;

}
