package org.sopt.makers.crew.main.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponseDto<T> {

  private final String errorCode;
  private T data;

  public static CommonResponseDto success(Object data) {
    return CommonResponseDto.builder()
        .data(data)
        .build();
  }


  public static CommonResponseDto fail(String errorCode) {
    return CommonResponseDto.builder()
        .errorCode(errorCode)
        .build();
  }
}
