package com.green.university.dto;

import lombok.Data;

@Data
public class MyEvaluationDto {
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

    public String answerSum() {
        double answerSum = (double)(answer1 + answer2 + answer3 + answer4 + answer5 + answer6 + answer7) / 7;
        String result = String.format("%.2f", answerSum);
        return result;
    }

}
