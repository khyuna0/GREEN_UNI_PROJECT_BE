package com.green.university.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomRestfullException extends RuntimeException {

	private HttpStatus status;
	
	public CustomRestfullException(String message, HttpStatus status) {
		super(message);
        if(status != null ) {
            this.status = status;
        }
	}
	
}
