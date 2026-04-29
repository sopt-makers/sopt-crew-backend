package org.sopt.makers.crew.main.entity.meeting.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum MeetingFrequency {
	LIGHT("가볍게"),
	STEADY("적당히"),
	IMMERSIVE("집중형");

	private static final String LEGACY_STEADY_VALUE = "꾸준히";

	@JsonValue
	private final String value;

	MeetingFrequency(String value) {
		this.value = value;
	}

	@JsonCreator
	public static MeetingFrequency ofValue(String dbData) {
		if (LEGACY_STEADY_VALUE.equals(dbData)) {
			return STEADY;
		}
		for (MeetingFrequency meetingFrequency : MeetingFrequency.values()) {
			if (meetingFrequency.getValue().equals(dbData)) {
				return meetingFrequency;
			}
		}
		throw new IllegalArgumentException("Invalid MeetingFrequency value: " + dbData);
	}
}
