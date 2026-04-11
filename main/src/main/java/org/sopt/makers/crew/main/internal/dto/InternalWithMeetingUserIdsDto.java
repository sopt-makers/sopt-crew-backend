package org.sopt.makers.crew.main.internal.dto;

import java.util.List;

public record InternalWithMeetingUserIdsDto(List<UserOrgIdDto> currentGenerationUserIds,
											List<UserOrgIdDto> pastGenerationUserIds) {

	public static InternalWithMeetingUserIdsDto of(List<Integer> currentGenerationUserIds,
		List<Integer> pastGenerationUserIds) {

		List<UserOrgIdDto> currentGenerationUserOrgIdDtos = currentGenerationUserIds.stream()
			.map(UserOrgIdDto::from)
			.toList();

		List<UserOrgIdDto> pastGenerationUserOrgIdDtos = pastGenerationUserIds.stream()
			.map(UserOrgIdDto::from)
			.toList();

		return new InternalWithMeetingUserIdsDto(currentGenerationUserOrgIdDtos, pastGenerationUserOrgIdDtos);
	}
}
