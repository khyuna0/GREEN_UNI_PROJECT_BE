package com.green.university.domain.evaluation.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EvaluationFormDto {
    // 강평 입력용 dto
    private Long answer1;
    private Long answer2;
    private Long answer3;
    private Long answer4;
    private Long answer5;
    private Long answer6;
    private Long answer7;
    @Size(max = 300, message = "강의 평가는 300자 미만으로 입력해 주세요")
    private String improvements;

}
