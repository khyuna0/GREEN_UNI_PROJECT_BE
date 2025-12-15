package com.green.university.domain.admin.controller;

import com.green.university.domain.admin.dto.LoginFormDto;
import com.green.university.domain.admin.dto.LoginResponseDto;
import com.green.university.domain.admin.service.UserService;
import com.green.university.global.exception.CustomRestfullException;
import com.green.university.global.security.CustomUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginFormDto loginFormDto,
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

        LoginResponseDto loginResponse = userService.login(loginFormDto); // JWT 발급

        // rememberId 쿠키 처리 , 선택사항이라 지워도 될,,껄?
        if ("on".equals(loginFormDto.getRememberId())) {
            Cookie cookie = new Cookie("id", loginFormDto.getId().toString());
            cookie.setMaxAge(60 * 60 * 24 * 7);
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            throw new CustomRestfullException("로그인 정보가 없습니다.", HttpStatus.UNAUTHORIZED);
        }
        Long id = principal.getId();
        String userRole = principal.getUserRole();
        String username = principal.getUsername();
        String name = (principal.getName() != null ? principal.getName() : "사용자" ); // 더미에 username없어서 만듬

        return ResponseEntity.ok(Map.of("id", id, "username", username, "role", userRole, "name", name));
    }

}
