package org.sopt.makers.crew.main.dining.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "DiningV2CreateRoundResponseDto", description = "회차 추가 응답 Dto")
public record DiningV2CreateRoundResponseDto(
	@Schema(description = "회차 id", example = "2")
	@NotNull
	Integer roundId
) {
	public static DiningV2CreateRoundResponseDto from(Integer roundId) {
		return new DiningV2CreateRoundResponseDto(roundId);
	}
}
