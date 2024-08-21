package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.common.dto.MeetingResponseDto;
import org.sopt.makers.crew.main.common.pagination.dto.PageMetaDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MeetingV2GetAllMeetingDto", description = "모임 조회 응답 Dto")
public record MeetingV2GetAllMeetingDto(
	@Schema(description = "모임 객체 목록", example = "")
	@NotNull
	List<MeetingResponseDto> meetings,
	@Schema(description = "페이지네이션 객체", example = "")
	@NotNull
	PageMetaDto meta
) {
	public static MeetingV2GetAllMeetingDto of(List<MeetingResponseDto> meetings, PageMetaDto meta){
		return new MeetingV2GetAllMeetingDto(meetings, meta);
	}
}
