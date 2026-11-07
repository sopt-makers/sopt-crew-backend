package org.sopt.makers.crew.main.global.exception;

import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends BaseException {
	public TooManyRequestsException() {
		super(HttpStatus.TOO_MANY_REQUESTS);
	}

	public TooManyRequestsException(String message) {
		super(HttpStatus.TOO_MANY_REQUESTS, message);
	}
}
