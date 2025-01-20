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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionAdvice {

	/**
	 * 400 Bad Request
	 * 클라이언트에서 잘못된 요청을 보냈을 때 처리하는 예외들
	 */
	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ExceptionResponse> handleBaseException(BaseException e) {
		log.warn("기본 예외 발생: {}", e.getMessage());
		return ResponseEntity.status(e.getStatusCode())
			.body(ExceptionResponse.fail(e.getErrorCode()));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ExceptionResponse> handleMissingParameter(MissingServletRequestParameterException e) {
		log.warn("누락된 파라미터: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.MISSING_REQUEST_PARAMETER.getErrorCode(),
				String.format("누락된 파라미터: %s", e.getParameterName())));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ExceptionResponse> handleIllegalArgument(IllegalArgumentException e) {
		log.warn("잘못된 인자: {}", e.getMessage());
		String errorDetails = String.format("예외 발생 위치: %s.%s - 인자: %s",
			e.getStackTrace()[0].getClassName(),
			e.getStackTrace()[0].getMethodName(),
			e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.INVALID_ARGUMENT.getErrorCode(),
				errorDetails));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ExceptionResponse> handleConstraintViolationException(ConstraintViolationException e) {
		log.warn("제약 조건 위반: {}", e.getMessage());

		StringBuilder violationMessages = new StringBuilder();
		for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
			violationMessages.append(String.format("필드: %s, 메시지: %s; ",
				violation.getPropertyPath(), violation.getMessage()));
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.CONSTRAINT_VIOLATION.getErrorCode(),
				violationMessages.toString()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ExceptionResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
		log.warn("데이터 무결성 위반: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.DATA_INTEGRITY_VIOLATION.getErrorCode(),
				e.getMostSpecificCause().getMessage()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
		log.warn("읽을 수 없는 메시지: {}", e.getMessage());
		String errorDetails = String.format("잘못된 JSON 요청: %s", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.MESSAGE_NOT_READABLE.getErrorCode(),
				errorDetails));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.warn("메서드 인자 유효성 검사 실패: {}", e.getMessage());
		StringBuilder errorDetails = new StringBuilder("유효성 검증 실패: ");
		e.getBindingResult().getFieldErrors().forEach(fieldError ->
			errorDetails.append(
				String.format("필드: '%s', 오류: '%s' ", fieldError.getField(), fieldError.getDefaultMessage())
			)
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.METHOD_ARGUMENT_NOT_VALID.getErrorCode(),
				errorDetails.toString()));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ExceptionResponse> handleMethodArgumentTypeMismatchException(
		MethodArgumentTypeMismatchException e) {
		log.warn("인자 타입 불일치: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.ARGUMENT_TYPE_MISMATCH.getErrorCode(),
				String.format("인자 타입 불일치: '%s', 잘못된 값: '%s'", e.getName(), e.getValue())));
	}

	@ExceptionHandler(MissingPathVariableException.class)
	public ResponseEntity<ExceptionResponse> handleMissingPathVariableException(MissingPathVariableException e) {
		log.warn("누락된 경로 변수: {}", e.getVariableName());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.MISSING_PATH_VARIABLE.getErrorCode(),
				String.format("누락된 경로 변수: %s", e.getVariableName())));
	}

	@ExceptionHandler(IOException.class)
	public ResponseEntity<ExceptionResponse> handleIOException(IOException e) {
		log.warn("입출력 예외: {}", e.getMessage());
		String errorMessage = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ExceptionResponse.fail(
				ErrorStatus.IO_EXCEPTION.getErrorCode(),
				String.format("입출력 예외 발생: %s", errorMessage)));
	}

	/**
	 * 405 Method Not Allowed
	 * 지원되지 않는 HTTP 메서드 처리
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ExceptionResponse> handleHttpRequestMethodNotSupportedException(
		HttpRequestMethodNotSupportedException e) {
		log.warn("지원되지 않는 요청 메서드: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
			.body(ExceptionResponse.fail(
				ErrorStatus.METHOD_NOT_SUPPORTED.getErrorCode(),
				String.format("지원되지 않는 메서드: %s", e.getMethod())));
	}

	/**
	 * 500 Internal Server Error
	 * 예상치 못한 예외 처리
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ExceptionResponse> handleException(Exception e) {
		log.error("예기치 않은 예외 발생: ", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ExceptionResponse.fail(
				ErrorStatus.INTERNAL_SERVER_ERROR.getErrorCode(),
				"예기치 않은 오류가 발생했습니다."));
	}
}
