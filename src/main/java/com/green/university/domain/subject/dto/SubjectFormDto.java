package com.green.university.domain.subject.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * @author 박성희
 *
 */
@Data
public class SubjectFormDto { // 강의 입력과 수정 시 사용하는 DTO
    private Long id;

    @NotEmpty
    @Size(min=2, max=20)
    private String name;

    //	@NotEmpty
//	@Min(10000000)
//	@Max(99999999)
    private Long professorId;
    private String professorName;

    //	@Size(max = 5)
    private String roomId;
    //	@NotEmpty
    private Long deptId;
    private String deptName;

    //	@NotEmpty
//	@Size(max = 2)
    private String type; // 전공 , 교양

    //	@NotEmpty
    private Long subYear; // 연도
    //	@NotEmpty
//	@Min(1)
//	@Max(2)
    private Long semester; // 학기
    //	@NotEmpty
//	@Size(max = 1)
    private String subDay; // 요일


    //	@NotEmpty
//	@Min(9)
//	@Max(18)
    private Long startTime; // 강의 시작 시간
    //	@NotEmpty
//	@Min(9)
//	@Max(18)
    private Long endTime; // 강의 종료 시간
    
    //	@NotEmpty
    private Long credits; // 이수 학점
    //	@NotEmpty
    private Long capacity; // 정원

}
