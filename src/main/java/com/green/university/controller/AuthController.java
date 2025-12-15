package com.green.university.controller;

import com.green.university.config.security.CustomUserDetails;
import com.green.university.dto.LoginDto;
import com.green.university.dto.response.LoginResponseDto;
import com.green.university.entity.User;
import com.green.university.exception.CustomRestfullException;
import com.green.university.service.UserService;
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

        // rememberId 쿠키 처리 - 굳이 백에서처리할 필요없음
//        if ("on".equals(loginDto.getRememberId())) {
//            Cookie cookie = new Cookie("id", loginDto.getId().toString());
//            cookie.setMaxAge(60 * 60 * 24 * 7);
//            cookie.setPath("/");
//            response.addCookie(cookie);
//        }

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
