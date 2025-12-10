package com.green.university.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 학생 출결 및 성적 기입 폼 stu_sub_detail_tb update용
 * 
 * @author 김지현
 */
@Data
public class UpdateStudentGradeDto {

    @NotNull(message = "결석 횟수를 입력해주세요.")
    private Long absent;

    @NotNull(message = "지각 횟수를 입력해주세요.")
    private Long lateness;

    @NotNull(message = "과제 점수를 입력해주세요.")
    private Long homework;

    @NotNull(message = "중간고사 점수를 입력해주세요.")
    private Long midExam;

    @NotNull(message = "기말고사 점수를 입력해주세요.")
    private Long finalExam;

    @NotNull(message = "환산 점수를 입력해주세요.")
    private Long convertedMark;

    @NotBlank(message = "등급을 선택해주세요.")
    private String grade;

}
