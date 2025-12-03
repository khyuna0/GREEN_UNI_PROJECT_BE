package com.green.university.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/error")
public class CustomErrorController {

    @GetMapping
    public ResponseEntity<?> handleError() {
        return ResponseEntity.status(404).body(Map.of(
                "error", true,
                "message", "요청하신 페이지를 찾을 수 없습니다."
        ));
    }
}
