package org.sopt.makers.crew.main.flash.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "FlashV2CreateAndUpdateResponseDto", description = "번쩍 모임 생성 및 수정 응답 Dto")
public record FlashV2CreateAndUpdateResponseDto(
	@Schema(description = "모임 id - 번쩍 카테고리", example = "1")
	@NotNull
	Integer meetingId,

	@Schema(description = "태그 id - 번쩍 카테고리", example = "1")
	@NotNull
	Integer tagId
) {
	public static FlashV2CreateAndUpdateResponseDto of(Integer meetingId, Integer tagId) {
		return new FlashV2CreateAndUpdateResponseDto(meetingId, tagId);
	}
}
