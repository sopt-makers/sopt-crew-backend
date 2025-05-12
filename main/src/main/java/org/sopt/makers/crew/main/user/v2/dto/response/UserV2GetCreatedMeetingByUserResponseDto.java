package org.sopt.makers.crew.main.user.v2.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UserV2GetCreatedMeetingByUserResponseDto", description = "내가 생성한 모임 조회 Dto")
public record UserV2GetCreatedMeetingByUserResponseDto(
	@Schema(description = "내가 생성한 모임 정보")
	@NotNull
	List<MeetingV2GetCreatedMeetingByUserResponseDto> meetings,
	@Schema(description = "내가 신청한 모임 갯수")
	@NotNull
	Integer count
) {
	public static UserV2GetCreatedMeetingByUserResponseDto from(
		List<MeetingV2GetCreatedMeetingByUserResponseDto> meetings) {
		return new UserV2GetCreatedMeetingByUserResponseDto(meetings, meetings.size());
	}
}
