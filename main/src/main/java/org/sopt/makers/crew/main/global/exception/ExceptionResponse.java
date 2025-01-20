package org.sopt.makers.crew.main.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionResponse {

	private final String errorCode;
	private final String message;

	public static ExceptionResponse fail(String errorCode) {
		return ExceptionResponse.builder()
			.errorCode(errorCode)
			.build();
	}

	public static ExceptionResponse fail(String errorCode, String message) {
		return ExceptionResponse.builder()
			.errorCode(errorCode)
			.message(message)
			.build();
	}
}
