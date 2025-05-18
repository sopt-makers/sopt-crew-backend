package org.sopt.makers.crew.main.meeting.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingV2CreateMeetingResponseDto", description = "일반 모임 생성 응답 Dto")
public class MeetingV2CreateMeetingResponseDto {

	@Schema(description = "모임 id", example = "1")
	@NotNull
	private Integer meetingId;

	@Schema(description = "태그 id - 일반 모임 카테고리", example = "1")
	@NotNull
	private Integer tagId;
}
