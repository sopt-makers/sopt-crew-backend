package org.sopt.makers.crew.main.entity.tag.enums;

import lombok.Getter;

@Getter
public enum WelcomeMessageType {
	YB_WELCOME("YB 환영"),
	OB_WELCOME("OB 환영"),
	FIRST_MEETING_WELCOME("초면 환영"),
	BEGINNER_WELCOME("입문자 환영"),
	EXPERIENCED_WELCOME("숙련자 환영");

	private final String value;

	WelcomeMessageType(String value) {
		this.value = value;
	}

	public static WelcomeMessageType ofValue(String dbData) {
		for (WelcomeMessageType type : WelcomeMessageType.values()) {
			if (type.getValue().equals(dbData)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Invalid WelcomeMessageType value: " + dbData);
	}
}
