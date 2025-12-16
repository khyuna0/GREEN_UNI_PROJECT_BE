package com.green.university.domain.grade.dto;

import lombok.Data;

/**
 * @author 서영
 * 장학금 유형 결정을 위한 성적을 가져오는 Dto
 */

@Data
public class GradeForScholarshipDto {

    private Long studentId;
    private Long subYear;
    private Long semester;
    private Double avgGrade;

}
