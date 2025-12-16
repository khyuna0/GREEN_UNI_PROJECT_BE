package com.green.university.domain.evaluation.dto;

import lombok.Data;

@Data
public class MyEvaluationFormDto {
    // 출력용 dto

    private Long professorId; //교수 id
    private String name;
    private Long answer1;
    private Long answer2;
    private Long answer3;
    private Long answer4;
    private Long answer5;
    private Long answer6;
    private Long answer7;
    private String improvements;
    private String answerSum;

    public void calculateAnswerSum() { // 총 평가 점수 계산
        double sum = (double)(answer1 + answer2 + answer3 + answer4 + answer5 + answer6 + answer7) / 7;
        this.answerSum = String.format("%.2f", sum);
    }

}
