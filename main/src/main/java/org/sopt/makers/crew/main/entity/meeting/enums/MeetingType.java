package org.sopt.makers.crew.main.entity.meeting.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum MeetingType {
	ONLINE("온라인"),
	OFFLINE("오프라인"),
	ONLINE_OFFLINE("온-오프");

	@JsonValue
	private final String value;

	MeetingType(String value) {
		this.value = value;
	}

	@JsonCreator
	public static MeetingType ofValue(String dbData) {
		for (MeetingType meetingType : MeetingType.values()) {
			if (meetingType.getValue().equals(dbData)) {
				return meetingType;
			}
		}
		throw new IllegalArgumentException("Invalid MeetingType value: " + dbData);
	}
}
