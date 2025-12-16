package com.green.university.dto;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;


@Data
public class LoginDto { // 로그인 유효성 검증용

    @Min(100000)
    @Max(2147483646)
    private Long id;
    @Size(min = 6, max = 20, message = "비밀번호는 6~20자 사이여야합니다.")
    private String password;
    //private String rememberId;

}
