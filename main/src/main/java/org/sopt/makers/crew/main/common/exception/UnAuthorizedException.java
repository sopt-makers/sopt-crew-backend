package org.sopt.makers.crew.main.common.exception;

import org.springframework.http.HttpStatus;

public class UnAuthorizedException extends BaseException {

	public UnAuthorizedException() {
		super(HttpStatus.UNAUTHORIZED);
	}

	public UnAuthorizedException(String responseMessage) {
		super(HttpStatus.UNAUTHORIZED, responseMessage);
	}
}
