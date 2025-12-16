package com.green.university.global;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/error")
public class CustomErrorController {

    @GetMapping
    public ResponseEntity<?> handleError() {
        return ResponseEntity.status(404).body(Map.of(
                "error", true,
                "message", "요청하신 페이지를 찾을 수 없습니다."
        ));
    }
}
