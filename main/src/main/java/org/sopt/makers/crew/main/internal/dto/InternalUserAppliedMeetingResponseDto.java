package org.sopt.makers.crew.main.internal.dto;

import java.util.List;

public record InternalUserAppliedMeetingResponseDto(List<UserAppliedMeetingDto> userAppliedMeetings) {

	public static InternalUserAppliedMeetingResponseDto from(List<UserAppliedMeetingDto> userAppliedMeetings) {
		return new InternalUserAppliedMeetingResponseDto(userAppliedMeetings);
	}
}
