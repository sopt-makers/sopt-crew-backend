package org.sopt.makers.crew.main.common.advice;

import org.sopt.makers.crew.main.common.exception.BaseException;
import org.sopt.makers.crew.main.common.response.CommonResponseDto;
import org.sopt.makers.crew.main.common.response.ErrorStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerExceptionAdvice {

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<CommonResponseDto> handleGlobalException(BaseException ex) {
    return ResponseEntity.status(ex.getStatusCode())
        .body(CommonResponseDto.fail(ex.getErrorCode()));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<CommonResponseDto> handleMissingParameter(
      MissingServletRequestParameterException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(CommonResponseDto.fail(
            ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION.getErrorCode()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<CommonResponseDto> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(CommonResponseDto.fail(ex.getMessage()));
  }

}
