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
    VALIDATION_EXCEPTION("잘못된 요청입니다."),
    VALIDATION_REQUEST_MISSING_EXCEPTION("요청값이 입력되지 않았습니다."),

    /**
     * 500 SERVER_ERROR
     */
    INTERNAL_SERVER_ERROR("예상치 못한 서버 에러가 발생했습니다."),
    BAD_GATEWAY_EXCEPTION("일시적인 에러가 발생하였습니다.\n잠시 후 다시 시도해주세요!");

    private final String message;

}