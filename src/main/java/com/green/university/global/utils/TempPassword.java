package com.green.university.global.utils;

/**
 * 임시 비밀번호 생성기
 * @author 김지현
 *
 */
public class TempPassword {
	
	private String password;
	
	public String returnTempPassword() {
		Long tempPassword = 0L;
		while(tempPassword < 100000) {
		 tempPassword = (long) (Math.random()*1000000);
		}
		password = tempPassword + "";
		System.out.println(password);
		return password;
	}

}
