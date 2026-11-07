package org.sopt.makers.crew.main.dining.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "DiningV2CreateDiningResponseDto", description = "회식 등록 응답 Dto")
public record DiningV2CreateDiningResponseDto(
	@Schema(description = "회식(모임) id", example = "1")
	@NotNull
	Integer meetingId
) {
	public static DiningV2CreateDiningResponseDto from(Integer meetingId) {
		return new DiningV2CreateDiningResponseDto(meetingId);
	}
}
