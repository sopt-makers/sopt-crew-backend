package org.sopt.makers.crew.main.global.exception;

public class CustomSlackException extends RuntimeException {
	public CustomSlackException() {
		super();
	}

	public CustomSlackException(String message) {
		super(message);
	}
}
