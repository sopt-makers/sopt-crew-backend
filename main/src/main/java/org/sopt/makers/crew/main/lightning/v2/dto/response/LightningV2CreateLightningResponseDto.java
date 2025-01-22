package org.sopt.makers.crew.main.lightning.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "LightningV2CreateLightningResponseDto", description = "번쩍 모임 생성 응답 Dto")
public record LightningV2CreateLightningResponseDto(
	@Schema(description = "모임 id - 번쩍 카테고리", example = "1")
	@NotNull
	Integer meetingId
) {
	public static LightningV2CreateLightningResponseDto from(Integer meetingId) {
		return new LightningV2CreateLightningResponseDto(meetingId);
	}
}
