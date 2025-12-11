package com.green.university.dto;

import com.green.university.entity.College;
import jakarta.validation.constraints.Size;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 
 * @author 박성희
 *
 */
@Data
public class RoomFormDto {
	@NotNull
	@Size(min = 4, max = 4)
	private String id;

	@NotNull
	private Long collegeId;
}
