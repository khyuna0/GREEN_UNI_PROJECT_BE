package com.green.university.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// 전역 에러 핸들러

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE) // 핸들러의 우선순위 높이는 어노테이션
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomRestfullException.class)
    public ResponseEntity<?> handleCustomRestfullException(CustomRestfullException ex) {

        return ResponseEntity
                .status(ex.getStatus())
                .body(Map.of(
                        "message", ex.getMessage()
                ));
    }
}
