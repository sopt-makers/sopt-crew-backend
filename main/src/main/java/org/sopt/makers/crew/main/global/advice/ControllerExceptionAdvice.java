package org.sopt.makers.crew.main.global.advice;

import java.io.IOException;

import org.sopt.makers.crew.main.global.exception.BaseException;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.sopt.makers.crew.main.global.exception.ExceptionResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionAdvice {

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ExceptionResponse> handleGlobalException(BaseException e) {
		log.warn("{}", e.getMessage());
		return ResponseEntity.status(e.getStatusCode())
			.body(ExceptionResponse.fail(e.getErrorCode()));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ExceptionResponse> handleMissingParameter(
		MissingServletRequestParameterException e) {
		log.warn("{}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION.getErrorCode()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ExceptionResponse> handleIllegalArgument(IllegalArgumentException e) {
		log.warn("{}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.INVALID_INPUT_VALUE.getErrorCode()));
	}

	@ExceptionHandler(ConstraintViolationException.class)  // @Notnull 오류
	public ResponseEntity<ExceptionResponse> handleConstraintViolationException(ConstraintViolationException e) {
		log.warn("{}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.INVALID_INPUT_VALUE.getErrorCode()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)  // null value 오류
	public ResponseEntity<ExceptionResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
		log.warn("{}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.INVALID_INPUT_VALUE.getErrorCode()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)  // null value 오류
	public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
		log.warn("{}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.INVALID_INPUT_VALUE.getErrorCode()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.warn("{}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.INVALID_INPUT_VALUE.getErrorCode()));
	}

	/**
	 * path variable errors
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ExceptionResponse> handleMethodArgumentTypeMismatchException(
		MethodArgumentTypeMismatchException e) {
		log.warn("{}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.INVALID_INPUT_VALUE.getErrorCode()));
	}

	@ExceptionHandler(MissingPathVariableException.class)
	public ResponseEntity<ExceptionResponse> handleMissingPathVariableException(
		MissingPathVariableException e) {
		log.warn("{}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.INVALID_INPUT_VALUE.getErrorCode()));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ExceptionResponse> handleHttpRequestMethodNotSupportedException(
		HttpRequestMethodNotSupportedException e) {
		log.warn("{}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.INVALID_INPUT_VALUE.getErrorCode()));
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<ExceptionResponse> handleIOException(IOException e) {
		log.warn("{}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.IO_EXCEPTION.getErrorCode()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponse> handleException(Exception e) {
		log.error("", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ExceptionResponse.fail(
				ErrorStatus.INTERNAL_SERVER_ERROR.getErrorCode()));
	}
}
