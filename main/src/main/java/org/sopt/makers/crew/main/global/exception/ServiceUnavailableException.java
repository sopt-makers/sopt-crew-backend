package org.sopt.makers.crew.main.global.exception;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends BaseException {

	public ServiceUnavailableException() {
		super(HttpStatus.SERVICE_UNAVAILABLE);
	}

	public ServiceUnavailableException(String message) {
		super(HttpStatus.SERVICE_UNAVAILABLE, message);
	}
}
