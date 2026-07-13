package org.sopt.makers.crew.main.meetingdemand.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingDemandV2CreateMeetingDemandResponseDto", description = "모임 수요 생성 응답 Dto")
public class MeetingDemandV2CreateMeetingDemandResponseDto {

	@Schema(description = "모임 수요 id", example = "1")
	@NotNull
	private Integer meetingDemandId;
}
