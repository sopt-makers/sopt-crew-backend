package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.global.dto.MeetingResponseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MeetingV2GetRecommendDto", description = "추천 모임 목록 조회 응답 Dto")
public record MeetingV2GetRecommendDto(
	@Schema(description = "모임 객체 목록", example = "")
	@NotNull
	List<MeetingResponseDto> meetings
) {
	public static MeetingV2GetRecommendDto from(List<MeetingResponseDto> meetings) {
		return new MeetingV2GetRecommendDto(meetings);
	}
}

