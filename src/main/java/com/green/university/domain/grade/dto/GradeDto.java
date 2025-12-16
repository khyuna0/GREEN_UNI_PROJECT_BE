package com.green.university.domain.grade.dto;

import lombok.Data;

@Data
public class GradeDto {

    private Long subYear;     // 수강년도
    private Long semester;     // 학기
    private Long subjectId;    // 과목 ID
    private Long evaluationId;  // 평가 ID
    private String name;      // 과목명 혹은 교수명 (기존 SQL에서 뭘 select 하느냐에 따라 다름)
    private String type;      // 전공/교양 등 타입
    private String letterGrade;      // 등급
    private String credits;     // 이수 학점
    private String gradePoint;  // 숫자형 학점

}
