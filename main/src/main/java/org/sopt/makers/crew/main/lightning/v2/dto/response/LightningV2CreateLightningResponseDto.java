package org.sopt.makers.crew.main.lightning.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "LightningV2CreateLightningResponseDto", description = "번쩍 모임 생성 응답 Dto")
public record LightningV2CreateLightningResponseDto(
	@Schema(description = "번쩍 모임 id", example = "1")
	@NotNull
	Integer lightningId
) {
	public static LightningV2CreateLightningResponseDto from(Integer lightningId) {
		return new LightningV2CreateLightningResponseDto(lightningId);
	}
}
