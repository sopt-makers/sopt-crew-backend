package org.sopt.makers.crew.main.lightning.v2.dto.request;

import java.util.List;

import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(name = "LightningV2CreateLightningBodyDto", description = "번쩍 모임 생성 및 수정 request body dto")
public record LightningV2CreateLightningBodyDto(
	@Schema(description = "번쩍 모임 생성 및 수정 request body")
	@NotNull LightningV2CreateLightningBodyWithoutWelcomeMessageDto lightningBody,

	@Schema(example = "[\"YB_WELCOME\", \"FIRST_MEETING_WELCOME\"]", description = "환영 메시지 타입 리스트")
	@NotEmpty List<WelcomeMessageType> welcomeMessageTypes
) {
}
