package org.sopt.makers.crew.main.entity.tag.enums;

import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WelcomeMessageType {
	YB_WELCOME("YB 환영"),
	OB_WELCOME("OB 환영"),
	FIRST_MEETING_WELCOME("초면 환영"),
	BEGINNER_WELCOME("입문자 환영"),
	EXPERIENCED_WELCOME("숙련자 환영");

	private final String value;

	public static WelcomeMessageType ofValue(String dbData) {
		for (WelcomeMessageType type : WelcomeMessageType.values()) {
			if (type.getValue().equals(dbData)) {
				return type;
			}
		}
		throw new BadRequestException(ErrorStatus.INVALID_WELCOME_MESSAGE_TYPE.getErrorCode());
	}
}
