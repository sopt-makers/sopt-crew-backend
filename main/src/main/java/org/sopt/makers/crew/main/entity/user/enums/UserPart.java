package org.sopt.makers.crew.main.entity.user.enums;

import java.util.Arrays;

import lombok.extern.slf4j.Slf4j;

import org.sopt.makers.crew.main.common.exception.BadRequestException;

@Slf4j
public enum UserPart {
	// 파트
	PM("기획"),
	DESIGN("디자인"),
	IOS("iOS"),
	ANDROID("안드로이드"),
	SERVER("서버"),
	WEB("웹"),

	// 파트장
	PM_LEADER("기획 파트장"),
	DESIGN_LEADER("디자인 파트장"),
	IOS_LEADER("iOS 파트장"),
	ANDROID_LEADER("안드로이드 파트장"),
	SERVER_LEADER("서버 파트장"),
	WEB_LEADER("웹 파트장"),

	// 임원진
	CHAIRMAN("회장"),
	VICE_CHAIRMAN("부회장"),
	GENERAL_AFFAIRS("총무"),
	OPERATION_LEADER("운영 팀장"),
	MEDIA_LEADER("미디어 팀장"),
	MAKERS_LEADER("메이커스 팀장");

	private final String value;

	UserPart(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static UserPart ofValue(String dbData) {
		return Arrays.stream(UserPart.values())
			.filter(v -> v.getValue().equals(dbData))
			.findFirst()
			.orElseThrow(() -> new BadRequestException(
				String.format("UserPart 클래스에 value = [%s] 값을 가진 enum 객체가 없습니다.", dbData)));
	}

}
