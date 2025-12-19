package com.green.university.domain.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class LoginFormDto { // 로그인 유효성 검증용
    
    @NotNull(message = "아이디를 입력해주세요" )
    private Long id;
    @Size(min = 6, max = 20, message = "비밀번호는 6~20자 사이로 입력해주세요")
    private String password;
    //private String rememberId;

}
