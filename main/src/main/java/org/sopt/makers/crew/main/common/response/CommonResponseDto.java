package org.sopt.makers.crew.main.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponseDto<T> {

  private final String errorCode;

  public static CommonResponseDto fail(String errorCode) {
    return CommonResponseDto.builder()
        .errorCode(errorCode)
        .build();
  }
}
