package org.sopt.makers.crew.main.meetingdemand.v2.dto.response;

import org.sopt.makers.crew.main.entity.user.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MeetingDemandV2OpenedMeetingCreatorResponseDto", description = "모임 수요 기반 개설 모임 개설자 응답 Dto")
public record MeetingDemandV2OpenedMeetingCreatorResponseDto(
	@Schema(description = "모임 개설자 id", example = "1")
	@NotNull
	Integer id,

	@Schema(description = "모임 개설자 이름", example = "홍길동")
	@NotNull
	String name,

	@Schema(description = "모임 개설자 프로필 이미지", example = "https://example.com/profile.png")
	String profileImage
) {
	public static MeetingDemandV2OpenedMeetingCreatorResponseDto of(User user) {
		return new MeetingDemandV2OpenedMeetingCreatorResponseDto(
			user.getId(),
			user.getName(),
			user.getProfileImage()
		);
	}
}
