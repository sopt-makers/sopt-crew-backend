package org.sopt.makers.crew.main.internal.dto;

import org.sopt.makers.crew.main.entity.meeting.Meeting;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TopFastestAppliedMeetingResponseDto", description = "가장 빠르게 신청한 모임 DTO")
public record TopFastestAppliedMeetingResponseDto(
	@Schema(description = "가장 빠르게 신청한 모임 id", example = "3")
	Integer meetingId,
	@Schema(description = "가장 빠르게 신청한 모임 이름", example = "가장 빠른 모임 1")
	String title
) {
	public static TopFastestAppliedMeetingResponseDto of(Meeting meeting) {
		return new TopFastestAppliedMeetingResponseDto(meeting.getId(), meeting.getTitle());
	}
}
