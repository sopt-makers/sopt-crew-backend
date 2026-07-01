package org.sopt.makers.crew.main.meetingdemand.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MeetingDemandV2GetMeetingDemandsResponseDto", description = "모임 수요 목록 조회 응답 Dto")
public record MeetingDemandV2GetMeetingDemandsResponseDto(
	@Schema(description = "모임 수요 목록")
	@NotNull
	List<MeetingDemandV2GetMeetingDemandSummaryResponseDto> meetingDemands,

	@Schema(description = "페이지네이션 객체")
	@NotNull
	PageMetaDto meta
) {
	public static MeetingDemandV2GetMeetingDemandsResponseDto of(
		List<MeetingDemandV2GetMeetingDemandSummaryResponseDto> meetingDemands, PageMetaDto meta) {
		return new MeetingDemandV2GetMeetingDemandsResponseDto(meetingDemands, meta);
	}
}
