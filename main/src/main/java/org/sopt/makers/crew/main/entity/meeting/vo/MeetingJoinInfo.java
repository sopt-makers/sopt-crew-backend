package org.sopt.makers.crew.main.entity.meeting.vo;

import org.sopt.makers.crew.main.entity.meeting.enums.MeetingFrequency;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MeetingJoinInfo(
	@JsonProperty("meetingType") MeetingType meetingType,
	@JsonProperty("meetingFrequency") MeetingFrequency meetingFrequency
) {
	@JsonCreator
	public MeetingJoinInfo(
		@JsonProperty("meetingType") MeetingType meetingType,
		@JsonProperty("meetingFrequency") MeetingFrequency meetingFrequency
	) {
		this.meetingType = meetingType;
		this.meetingFrequency = meetingFrequency;
	}

	@JsonIgnore
	public boolean isEmpty() {
		return meetingType == null && meetingFrequency == null;
	}

	@JsonIgnore
	public boolean hasMissingValue() {
		return meetingType == null || meetingFrequency == null;
	}
}
