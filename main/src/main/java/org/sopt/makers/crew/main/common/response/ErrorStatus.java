package org.sopt.makers.crew.main.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)

public enum ErrorStatus {
  /**
   * 400 BAD_REQUEST
   */
  VALIDATION_EXCEPTION("CR-001"), // errorCode는 예시, 추후 변경 예정 -> 잘못된 요청입니다.
  VALIDATION_REQUEST_MISSING_EXCEPTION("요청값이 입력되지 않았습니다."),

  /**
   * 401 UNAUTHORIZED
   */
  UNAUTHORIZED_TOKEN("유효하지 않은 토큰입니다."),

  /**
   * 500 SERVER_ERROR
   */
  INTERNAL_SERVER_ERROR("예상치 못한 서버 에러가 발생했습니다.");

  private final String errorCode;

}