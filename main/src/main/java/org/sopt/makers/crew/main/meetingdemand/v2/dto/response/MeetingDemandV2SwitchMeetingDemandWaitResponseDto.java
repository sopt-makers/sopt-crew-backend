package org.sopt.makers.crew.main.meetingdemand.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingDemandV2SwitchMeetingDemandWaitResponseDto", description = "모임 수요 기다려요 토글 응답 Dto")
public class MeetingDemandV2SwitchMeetingDemandWaitResponseDto {

	@Schema(description = "기다려요 수", example = "10")
	@NotNull
	private int waitCount;

	@Schema(description = "요청 후 본인이 기다려요를 누른 상태", example = "true")
	@NotNull
	private Boolean isWaiting;
}
