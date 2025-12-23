package org.sopt.makers.crew.main.soptmap.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.global.pagination.dto.PageMetaDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "SoptMapGetAllDto", description = "솝맵 목록 조회 응답 Dto")
public record SoptMapGetAllDto(
	@Schema(description = "솝맵 목록") @NotNull List<SoptMapListResponseDto> soptMaps,
	@Schema(description = "페이지네이션 정보") @NotNull PageMetaDto meta) {
	public static SoptMapGetAllDto of(List<SoptMapListResponseDto> soptMaps, PageMetaDto meta) {
		return new SoptMapGetAllDto(soptMaps, meta);
	}
}
