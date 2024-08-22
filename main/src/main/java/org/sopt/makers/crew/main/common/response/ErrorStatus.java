package org.sopt.makers.crew.main.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorStatus {
    /**
     * 204 NO_CONTENT
     */
    NO_CONTENT_EXCEPTION("참여한 모임이 없습니다."),

    /**
     * 400 BAD_REQUEST
     */
    VALIDATION_EXCEPTION("CF-001"),
    VALIDATION_REQUEST_MISSING_EXCEPTION("요청값이 입력되지 않았습니다."),
    NOT_FOUND_MEETING("모임이 없습니다."),
    NOT_FOUND_POST("존재하지 않는 게시글입니다."),
    NOT_FOUND_COMMENT("존재하지 않는 댓글입니다."),
    FULL_MEETING_CAPACITY("정원이 꽉 찼습니다."),
    ALREADY_APPLIED_MEETING("이미 지원한 모임입니다."),
    ALREADY_REPORTED_COMMENT("이미 신고한 댓글입니다."),
    NOT_IN_APPLY_PERIOD("모임 지원 기간이 아닙니다."),
    MISSING_GENERATION_PART("내 프로필에서 기수/파트 정보를 입력해주세요."),
    NOT_ACTIVE_GENERATION("활동 기수가 아닙니다."),
    NOT_TARGET_PART("지원 가능한 파트가 아닙니다."),
    NOT_FOUND_APPLY("신청상태가 아닌 모임입니다."),

    /**
     * 401 UNAUTHORIZED
     */
    UNAUTHORIZED_TOKEN("유효하지 않은 토큰입니다."),
    UNAUTHORIZED_USER("존재하지 않거나 유효하지 않은 유저입니다."),

    /**
     * 403 FORBIDDEN
     */
    FORBIDDEN_EXCEPTION("권한이 없습니다."),

    /**
     * 500 SERVER_ERROR
     */
    NOTIFICATION_SERVER_ERROR("알림 서버에 에러가 발생했습니다."),
    INTERNAL_SERVER_ERROR("예상치 못한 서버 에러가 발생했습니다.");

    private final String errorCode;

}