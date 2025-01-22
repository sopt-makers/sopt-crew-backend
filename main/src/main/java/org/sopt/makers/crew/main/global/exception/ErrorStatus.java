package org.sopt.makers.crew.main.global.exception;

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
	 * 400 BAD_REQUEST - 비즈니스 로직 관련 에러
	 */
	VALIDATION_EXCEPTION("CF-001"),
	VALIDATION_REQUEST_MISSING_EXCEPTION("요청값이 입력되지 않았습니다."),
	INVALID_INPUT_VALUE_FILTER("요청값 또는 토큰이 올바르지 않습니다."),
	FULL_MEETING_CAPACITY("정원이 꽉 찼습니다."),
	ALREADY_APPLIED_MEETING("이미 지원한 모임입니다."),
	ALREADY_REPORTED_COMMENT("이미 신고한 댓글입니다."),
	ALREADY_REPORTED_POST("이미 신고한 게시글입니다."),
	NOT_IN_APPLY_PERIOD("모임 지원 기간이 아닙니다."),
	MISSING_GENERATION_PART("내 프로필에서 기수/파트 정보를 입력해주세요."),
	NOT_ACTIVE_GENERATION("활동 기수가 아닙니다."),
	NOT_TARGET_PART("지원 가능한 파트가 아닙니다."),
	NOT_FOUND_APPLY("신청상태가 아닌 모임입니다."),
	ALREADY_PROCESSED_APPLY("이미 해당 상태로 처리된 신청 정보입니다."),
	MAX_IMAGE_UPLOAD_EXCEEDED("이미지는 최대 10개까지만 업로드 가능합니다."),
	LEADER_CANNOT_APPLY("모임장은 신청할 수 없습니다."),
	CO_LEADER_CANNOT_APPLY("공동 모임장은 신청할 수 없습니다."),
	LEADER_CANNOT_BE_CO_LEADER_APPLY("모임장은 공동 모임장이 될 수 없습니다."),
	NOT_ALLOW_MEETING_APPLY("허용되지 않는 모임 신청입니다."),
	IO_EXCEPTION("파일 입출력 오류가 발생했습니다."),

	/**
	 * 400 BAD_REQUEST - 유효성 검사 관련 에러
	 */
	MISSING_REQUEST_PARAMETER("필수 요청 파라미터가 누락되었습니다."),
	METHOD_ARGUMENT_NOT_VALID("메서드 인자 유효성 검사가 실패했습니다."),
	ARGUMENT_TYPE_MISMATCH("인자 타입이 일치하지 않습니다."),
	MISSING_PATH_VARIABLE("필수 경로 변수가 누락되었습니다."),
	INVALID_ARGUMENT("잘못된 인자입니다."),
	CONSTRAINT_VIOLATION("제약 조건을 위반했습니다."),
	DATA_INTEGRITY_VIOLATION("데이터 무결성이 위반되었습니다."),
	MESSAGE_NOT_READABLE("읽을 수 없는 메시지 형식입니다."),
	INVALID_INPUT_VALUE("요청값이 올바르지 않습니다. : "),

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
	 * 404 NOT_FOUND
	 */
	NOT_FOUND_MEETING("모임이 없습니다."), // 예외 처리 NotFound로 수정 필요
	NOT_FOUND_POST("존재하지 않는 게시글입니다."), // 예외 처리 NotFound로 수정 필요
	NOT_FOUND_USER("존재하지 않는 유저입니다."), // 예외 처리 NotFound로 수정 필요
	NOT_FOUND_COMMENT("존재하지 않는 댓글입니다."), // 예외 처리 NotFound로 수정 필요
	NOT_FOUND_LIGHTNING("번쩍 모임이 없습니다."), // 예외 처리 NotFound로 수정 필요

	/**
	 * 405 METHOD_NOT_ALLOWED
	 */
	METHOD_NOT_SUPPORTED("지원되지 않는 HTTP 메서드입니다."),

	/**
	 * 500 SERVER_ERROR
	 */
	NOTIFICATION_SERVER_ERROR("알림 서버에 에러가 발생했습니다."),
	CSV_ERROR("csv 처리 과정에 에러가 발생했습니다."),
	S3_STORAGE_ERROR("s3 스토리지에 에러가 발생했습니다."),
	INTERNAL_SERVER_ERROR("예상치 못한 서버 에러가 발생했습니다.");

	private final String errorCode;

}
