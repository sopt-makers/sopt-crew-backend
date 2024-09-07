package org.sopt.makers.crew.main.user.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "ApplyV2GetAppliedMeetingByUserResponseDto", description = "내가 신청한 모임 Dto")
public record ApplyV2GetAppliedMeetingByUserResponseDto(
	@Schema(description = "신청 id", example = "130")
	@NotNull
	Integer id,
	@Schema(description = "신청 상태", example = "1", type = "number", allowableValues = {"0", "1", "2"})
	@NotNull
	Integer status,
	@Schema(description = "신청 모임 정보")
	@NotNull
	MeetingV2GetCreatedMeetingByUserResponseDto meeting
) {
	public static ApplyV2GetAppliedMeetingByUserResponseDto of(Integer id, Integer status,
		MeetingV2GetCreatedMeetingByUserResponseDto meeting) {
		return new ApplyV2GetAppliedMeetingByUserResponseDto(id, status, meeting);
	}
}
