package org.sopt.makers.crew.main.meeting.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingV2ApplyMeetingResponseDto", description = "모임 신청 응답 Dto")
public class MeetingV2ApplyMeetingResponseDto {

	@Schema(description = "신청 id", example = "1")
	@NotNull
	private Integer applyId;

}
