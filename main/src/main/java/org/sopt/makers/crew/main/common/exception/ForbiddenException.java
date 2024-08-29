package org.sopt.makers.crew.main.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {

	public ForbiddenException() {
		super(HttpStatus.FORBIDDEN);
	}

	public ForbiddenException(String message) {
		super(HttpStatus.FORBIDDEN, message);
	}

}
