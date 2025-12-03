package com.green.university.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

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
	@NotEmpty
	@Min(10000000)
	@Max(99999999)
	private Long professorId;
	@Size(max = 5)
	private String roomId;
	@NotEmpty
	private Long deptId;
	@NotEmpty
	@Size(max = 2)
	private String type;
	@NotEmpty
	private Long subYear;
	@NotEmpty
	@Min(1)
	@Max(2)
	private Long semester;
	@NotEmpty
	@Size(max = 1)
	private String subDay;
	@NotEmpty
	@Min(9)
	@Max(18)
	private Long startTime;
	@NotEmpty
	@Min(9)
	@Max(18)
	private Long endTime;
	@NotEmpty
	private Long grades;
	@NotEmpty
	private Long capacity;
	private Long numOfStudent;
}
