package org.sopt.makers.crew.main.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionResponse {

  private final String errorCode;

  public static ExceptionResponse fail(String errorCode) {
    return ExceptionResponse.builder()
        .errorCode(errorCode)
        .build();
  }
}
