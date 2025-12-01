package com.green.university.dto;

import com.green.university.utils.NumberUtil;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
/**
 * 
 * @author 박성희
 *
 */
@Data
public class CollTuitFormDto {
	@NotBlank
	private Long collegeId;
	private String 	name;
	@NotEmpty
	private Long amount;

	public String amountFormat() {
		return NumberUtil.numberFormat(amount);
	}
	
}
