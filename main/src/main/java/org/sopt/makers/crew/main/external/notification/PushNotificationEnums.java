package org.sopt.makers.crew.main.external.notification;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum PushNotificationEnums {
	PUSH_NOTIFICATION_ACTION("send"),

	PUSH_NOTIFICATION_CATEGORY("NEWS"),

	// 생성 POST API(createPost, createComment, createFlash) 호출 시 알림 서버도 호출
	NEW_POST_PUSH_NOTIFICATION_TITLE("✏️내 모임에 새로운 글이 업로드됐어요."),
	NEW_COMMENT_PUSH_NOTIFICATION_TITLE("💬나의 모임 피드에 새로운 댓글이 달렸어요."),
	NEW_FLASH_PUSH_NOTIFICATION_TITLE("⚡️새로운 번쩍 모임이 열렸어요!"),

	// 유저가 관심가진 keyword 모임, 번쩍 create 호출 시 알림 서버 호출
	// TODO : 주현이가 준 키워드 문구로 변경 진행! 슬랙 참고
	NEW_INTEREST_KEYWORD_PUSH_NOTIFICATION_TITLE("내가 관심을 가진 키워드의 모임글이 생겼어요!"),

	// 별도로 분리된 mention 관련 API 호출 시 알림 서버 호출
	NEW_POST_MENTION_PUSH_NOTIFICATION_TITLE("✏️모임 피드에서 회원님이 언급됐어요."),
	NEW_COMMENT_MENTION_PUSH_NOTIFICATION_TITLE("💬%s님이 회원님을 언급했어요."),

	;
	private final String value;
}
