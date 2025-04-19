package org.sopt.makers.crew.main.flash.v2.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "FlashV2CreateAndUpdateFlashBodyDto", description = "번쩍 모임 생성 및 수정 request body dto")
public record FlashV2CreateAndUpdateFlashBodyDto(
	@Schema(description = "번쩍 모임 생성 및 수정 request body")
	@NotNull
	@Valid
	FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto flashBody,

	@Schema(example = "[\"YB 환영\", \"OB 환영\"]", description = "환영 메시지 타입 리스트")
	List<String> welcomeMessageTypes,

	@Schema(example = "[\"운동\", \"자기개발\"]", description = "모임 키워드 타입 리스트")
	@Size(min = 1, max = 2)
	List<String> meetingKeywordTypes
) {
}
