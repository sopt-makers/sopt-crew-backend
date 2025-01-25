package org.sopt.makers.crew.main.flash.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "FlashV2CreateFlashResponseDto", description = "번쩍 모임 생성 응답 Dto")
public record FlashV2CreateFlashResponseDto(
	@Schema(description = "모임 id - 번쩍 카테고리", example = "1")
	@NotNull
	Integer meetingId
) {
	public static FlashV2CreateFlashResponseDto from(Integer meetingId) {
		return new FlashV2CreateFlashResponseDto(meetingId);
	}
}
