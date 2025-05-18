package org.sopt.makers.crew.main.tag.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "TagV2CreateGeneralMeetingTagResponseDto", description = "일반 모임 태그 생성 응답 Dto")
public record TagV2CreateGeneralMeetingTagResponseDto(
	@Schema(description = "모임 태그 id", example = "1")
	@NotNull
	Integer tagId
) {
	public static TagV2CreateGeneralMeetingTagResponseDto from(Integer tagId) {
		return new TagV2CreateGeneralMeetingTagResponseDto(tagId);
	}
}
