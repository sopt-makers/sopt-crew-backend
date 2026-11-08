package org.sopt.makers.crew.main.dining.v2.service;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;

/**
 * 회원 활동 이력(activities.part)의 자유 표기를 회식 참여 파트로 변환한다.
 * 플레이그라운드 데이터에는 "서버 파트장", "ios", "백엔드"처럼 파트장 표기·대소문자·별칭이 섞여 있다.
 * 회장, 총무, 메이커스 팀장 같은 역할 값은 파트가 아니므로 변환하지 않는다. (같은 기수의 파트 값이 함께 저장돼 있음)
 */
public final class DiningUserPartResolver {

	private static final String LEADER_SUFFIX = "파트장";

	private static final Map<String, MeetingJoinablePart> PARTS = Map.of(
		"기획", MeetingJoinablePart.PM,
		"pm", MeetingJoinablePart.PM,
		"디자인", MeetingJoinablePart.DESIGN,
		"ios", MeetingJoinablePart.IOS,
		"안드로이드", MeetingJoinablePart.ANDROID,
		"서버", MeetingJoinablePart.SERVER,
		"백엔드", MeetingJoinablePart.SERVER,
		"웹", MeetingJoinablePart.WEB,
		"프론트엔드", MeetingJoinablePart.WEB
	);

	private DiningUserPartResolver() {
	}

	public static Optional<MeetingJoinablePart> resolve(String part) {
		if (part == null) {
			return Optional.empty();
		}
		String normalized = part.replace(" ", "").toLowerCase(Locale.ROOT);
		if (normalized.endsWith(LEADER_SUFFIX)) {
			normalized = normalized.substring(0, normalized.length() - LEADER_SUFFIX.length());
		}
		return Optional.ofNullable(PARTS.get(normalized));
	}
}
