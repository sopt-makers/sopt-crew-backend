package org.sopt.makers.crew.main.internal.dto;

import java.util.List;

import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;

public record InternalPostGetAllResponseDto(List<InternalPostResponseDto> posts, PageMetaDto pageMeta) {

	public static InternalPostGetAllResponseDto from(List<InternalPostResponseDto> posts, PageMetaDto pageMeta) {
		return new InternalPostGetAllResponseDto(posts, pageMeta);
	}
}
