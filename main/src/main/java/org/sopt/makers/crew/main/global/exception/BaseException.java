package org.sopt.makers.crew.main.global.exception;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseException extends RuntimeException {

	private HttpStatus httpStatus;
	private String errorCode;

	public BaseException(HttpStatus httpStatus) {
		super();
		this.httpStatus = httpStatus;
	}

	public BaseException(HttpStatus httpStatus, String errorCode) {
		super(errorCode);
		this.httpStatus = httpStatus;
		this.errorCode = errorCode;
	}

	public int getStatusCode() {
		return this.httpStatus.value();
	}
}
