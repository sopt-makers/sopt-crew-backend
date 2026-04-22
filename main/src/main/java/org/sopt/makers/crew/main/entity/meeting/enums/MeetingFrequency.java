package org.sopt.makers.crew.main.entity.meeting.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum MeetingFrequency {
	LIGHT("가볍게"),
	// TODO: 추후 스펙 크로스 체크 후 변경 예정
	STEADY("꾸준히"),
	IMMERSIVE("몰입형");

	@JsonValue
	private final String value;

	MeetingFrequency(String value) {
		this.value = value;
	}

	@JsonCreator
	public static MeetingFrequency ofValue(String dbData) {
		for (MeetingFrequency meetingFrequency : MeetingFrequency.values()) {
			if (meetingFrequency.getValue().equals(dbData)) {
				return meetingFrequency;
			}
		}
		throw new IllegalArgumentException("Invalid MeetingFrequency value: " + dbData);
	}
}
