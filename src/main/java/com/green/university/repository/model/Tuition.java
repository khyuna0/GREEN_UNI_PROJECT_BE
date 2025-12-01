package com.green.university.repository.model;

import com.green.university.utils.NumberUtil;
import lombok.Data;

/**
 * @author 서영
 *
 */

@Data
public class Tuition {

	private Long studentId;
	private Long tuiYear;
	private Long semester;
	private Long tuiAmount;
	private Long schType;
	private Long schAmount;
	private Boolean status;
	
	/**
	 * @return 금액 형식으로 변환한 등록금
	 */
	public String tuiFormat() {
		return NumberUtil.numberFormat(tuiAmount);
	}
	
	/**
	 * @return 금액 형식으로 변환한 장학금
	 */
	public String schFormat() {
		return NumberUtil.numberFormat(schAmount);
	}
	
	/**
	 * @return 금액 형식으로 변환한 납부금
	 */
	public String paymentFormat() {
		Long payAmount = tuiAmount - schAmount;
		return NumberUtil.numberFormat(payAmount);
	}

	public Tuition(Long studentId, Long tuiYear, Long semester, Long tuiAmount, Long schType, Long schAmount) {
		super();
		this.studentId = studentId;
		this.tuiYear = tuiYear;
		this.semester = semester;
		this.tuiAmount = tuiAmount;
		this.schType = schType;
		this.schAmount = schAmount;
	}
	
	
	
}
