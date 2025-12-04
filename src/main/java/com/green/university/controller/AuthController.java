package com.green.university.controller;

import com.green.university.dto.LoginDto;
import com.green.university.dto.response.LoginResponseDto;
import com.green.university.handler.exception.CustomRestfullException;
import com.green.university.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginDto loginDto,
            BindingResult bindingResult,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder();
            bindingResult.getAllErrors().forEach(error -> {
                sb.append(error.getDefaultMessage()).append("\\n");
            });
            throw new CustomRestfullException(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        LoginResponseDto loginResponse = userService.login(loginDto); // JWT 발급

        // rememberId 쿠키 처리 , 선택사항이라 지워도 될,,껄?
        if ("on".equals(loginDto.getRememberId())) {
            Cookie cookie = new Cookie("id", loginDto.getId().toString());
            cookie.setMaxAge(60 * 60 * 24 * 7);
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        return ResponseEntity.ok(loginResponse);
    }

}
