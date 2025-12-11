package com.green.university.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Min(value = 0, message = "결석 횟수는 0 이상이어야 합니다.")
    private Long absent;

    @NotNull(message = "지각 횟수를 입력해주세요.")
    @Min(value = 0, message = "지각 횟수는 0 이상이어야 합니다.")
    private Long lateness;

    @NotNull(message = "과제 점수를 입력해주세요.")
    @Min(value = 0, message = "점수는 0점 이상이어야 합니다.")
    @Max(value = 100, message = "점수는 100점 이하여야 합니다.")
    private Long homework;

    @NotNull(message = "중간고사 점수를 입력해주세요.")
    @Min(value = 0, message = "점수는 0점 이상이어야 합니다.")
    @Max(value = 100, message = "점수는 100점 이하여야 합니다.")
    private Long midExam;

    @NotNull(message = "기말고사 점수를 입력해주세요.")
    @Min(value = 0, message = "점수는 0점 이상이어야 합니다.")
    @Max(value = 100, message = "점수는 100점 이하여야 합니다.")
    private Long finalExam;

    private Long convertedMark;

    private String grade;

}
