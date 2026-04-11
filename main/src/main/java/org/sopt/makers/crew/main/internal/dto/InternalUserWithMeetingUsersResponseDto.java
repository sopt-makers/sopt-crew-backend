package org.sopt.makers.crew.main.internal.dto;

import java.util.List;

public record InternalUserWithMeetingUsersResponseDto(List<UserOrgIdDto> currentGenerationUserIds,
													  List<UserOrgIdDto> pastGenerationUserIds) {

	public static InternalUserWithMeetingUsersResponseDto from(
		InternalWithMeetingUserIdsDto internalWithMeetingUserIdsDto) {

		return new InternalUserWithMeetingUsersResponseDto(
			internalWithMeetingUserIdsDto.currentGenerationUserIds(),
			internalWithMeetingUserIdsDto.pastGenerationUserIds()
		);
	}
}
