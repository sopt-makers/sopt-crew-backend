package org.sopt.makers.crew.main.entity.meeting.vo;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import org.sopt.makers.crew.main.entity.meeting.enums.MeetingFrequency;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MeetingJoinInfo(
	@JsonProperty("meetingType") MeetingType meetingType,
	@JsonProperty("meetingFrequency") MeetingFrequency meetingFrequency
) {
	@JsonCreator
	public MeetingJoinInfo {
		if (meetingType == null || meetingFrequency == null) {
			throw new IllegalArgumentException(INVALID_INPUT_VALUE.getErrorCode());
		}
	}
}
