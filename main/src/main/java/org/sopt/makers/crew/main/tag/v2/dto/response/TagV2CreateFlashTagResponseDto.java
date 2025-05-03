package org.sopt.makers.crew.main.tag.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "TagV2CreateFlashTagResponseDto", description = "번쩍 모임 태그 생성 응답 Dto")
public record TagV2CreateFlashTagResponseDto(
	@Schema(description = "모임 태그 id", example = "1")
	@NotNull
	Integer tagId
) {
	public static TagV2CreateFlashTagResponseDto from(Integer tagId) {
		return new TagV2CreateFlashTagResponseDto(tagId);
	}
}
