package org.sopt.makers.crew.main.global.exception;

import org.springframework.http.HttpStatus;

public class LockedException extends BaseException {
	public LockedException() {
		super(HttpStatus.LOCKED);
	}

	public LockedException(String message) {
		super(HttpStatus.LOCKED, message);
	}
}
