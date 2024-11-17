package org.sopt.makers.crew.main.internal.dto;

import java.util.List;

import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "InternalMeetingGetAllMeetingDto", description = "[Internal] 모임 목록 조회 응답 Dto")
public record InternalMeetingGetAllMeetingDto(
	@Schema(description = "모임 객체 목록", example = "")
	@NotNull
	List<InternalMeetingResponseDto> meetings,
	@Schema(description = "페이지네이션 객체", example = "")
	@NotNull
	PageMetaDto meta
) {
	public static InternalMeetingGetAllMeetingDto of(List<InternalMeetingResponseDto> meetings, PageMetaDto meta) {
		return new InternalMeetingGetAllMeetingDto(meetings, meta);
	}
}
