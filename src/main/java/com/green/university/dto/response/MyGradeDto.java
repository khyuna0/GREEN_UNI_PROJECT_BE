package com.green.university.dto.response;

import lombok.Data;

@Data
public class MyGradeDto {
    private Long studentId;
    private Long subYear;
    private Long semester;
    private Long sumGrades; // 이수 해야할 학점
    private Long myGrades;  // 내가 실제로 이수한 학점
    private float average;  // 평균 평점

    public String average() {
        String result = String.format("%.2f", average);
        return result;
    }

}
