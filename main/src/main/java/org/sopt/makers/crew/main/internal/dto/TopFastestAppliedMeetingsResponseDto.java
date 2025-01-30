package org.sopt.makers.crew.main.internal.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TopFastestAppliedMeetingsResponseDto", description = "가장 빠르게 신청한 모임 목록 DTO")
public record TopFastestAppliedMeetingsResponseDto(
	@Schema(description = "가장 빠르게 신청한 모임", example = "")
	List<TopFastestAppliedMeetingResponseDto> topFastestAppliedMeetings
) {
	public static TopFastestAppliedMeetingsResponseDto from(
		List<TopFastestAppliedMeetingResponseDto> topFastestAppliedMeetings) {

		return new TopFastestAppliedMeetingsResponseDto(topFastestAppliedMeetings);
	}
}
