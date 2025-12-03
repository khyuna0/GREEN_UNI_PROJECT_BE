package com.green.university.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/error")
public class CustomErrorController {

	@GetMapping("/")
	public ResponseEntity<?> handleError() {
		return "/error/errorPage";
	}

}
