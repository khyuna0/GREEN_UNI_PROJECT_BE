package com.green.university.dto;

import lombok.Data;

/**
 * 교수 리스트 보기 폼
 * @author 김지현
 */
@Data
public class ProfessorListForm {

    // 검색어가 있는 경우, 검색어를 이 DTO에 넣어 찾는다
	private Long deptId;
	private Long professorId;
	private int page;
	
}
