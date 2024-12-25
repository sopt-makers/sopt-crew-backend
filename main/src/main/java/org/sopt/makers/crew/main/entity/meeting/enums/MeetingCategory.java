package org.sopt.makers.crew.main.entity.meeting.enums;

import java.util.Arrays;

import org.sopt.makers.crew.main.global.exception.BadRequestException;

public enum MeetingCategory {
	STUDY("스터디"),
	LECTURE("강연"),
	LIGHTNING("번쩍"),
	EVENT("행사"),
	SEMINAR("세미나");

	private final String value;

	MeetingCategory(String value) {
		this.value = value;
	}

	public static MeetingCategory ofValue(String dbData) {
		return Arrays.stream(MeetingCategory.values()).filter(v -> v.getValue().equals(dbData))
			.findFirst().orElseThrow(() -> new BadRequestException(
				String.format("MeetingCategory 클래스에 value = [%s] 값을 가진 enum 객체가 없습니다.", dbData)));
	}

	public String getValue() {
		return value;
	}
}