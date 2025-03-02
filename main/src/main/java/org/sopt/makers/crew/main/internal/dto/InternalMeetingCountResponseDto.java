package org.sopt.makers.crew.main.internal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InternalMeetingCountResponseDto", description = "[Internal] 특정 기수 모임 개수 응답 Dto")
public record InternalMeetingCountResponseDto(
	@Schema(description = "모임 개수", example = "103")
	int meetingCount
) {
	public static InternalMeetingCountResponseDto from(int meetingCount) {
		return new InternalMeetingCountResponseDto(meetingCount);
	}
}
