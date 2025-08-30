package org.sopt.makers.crew.main.internal.dto;

import java.util.List;

import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;

public record InternalMeetingGetAllWritingPostResponseDto(
	List<InternalMeetingForWritingPostDto> meetings,
	PageMetaDto pageMetaData
) {
	public static InternalMeetingGetAllWritingPostResponseDto from(List<InternalMeetingForWritingPostDto> meetings,
		PageMetaDto metaDto) {
		return new InternalMeetingGetAllWritingPostResponseDto(meetings, metaDto);
	}
}
