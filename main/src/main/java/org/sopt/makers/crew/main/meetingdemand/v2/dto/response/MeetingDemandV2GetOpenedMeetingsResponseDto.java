package org.sopt.makers.crew.main.meetingdemand.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MeetingDemandV2GetOpenedMeetingsResponseDto", description = "모임 수요 기반 개설 모임 목록 조회 응답 Dto")
public record MeetingDemandV2GetOpenedMeetingsResponseDto(
	@Schema(description = "해당 모임 수요 기반으로 개설된 전체 모임 수", example = "3")
	@NotNull
	int openedMeetingCount,

	@Schema(description = "해당 모임 수요 기반으로 개설된 모임 카드 목록")
	@NotNull
	List<MeetingDemandV2GetOpenedMeetingResponseDto> meetings,

	@Schema(description = "페이지네이션 객체")
	@NotNull
	PageMetaDto meta
) {
	public static MeetingDemandV2GetOpenedMeetingsResponseDto of(
		int openedMeetingCount, List<MeetingDemandV2GetOpenedMeetingResponseDto> meetings, PageMetaDto meta) {
		return new MeetingDemandV2GetOpenedMeetingsResponseDto(openedMeetingCount, meetings, meta);
	}
}
