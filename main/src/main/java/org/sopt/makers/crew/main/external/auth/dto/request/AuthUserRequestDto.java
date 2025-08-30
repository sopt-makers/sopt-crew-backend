package org.sopt.makers.crew.main.external.auth.dto.request;

import java.util.List;
import java.util.stream.Collectors;

public record AuthUserRequestDto(
	String userId
) {
	public static AuthUserRequestDto from(Integer userId) {
		return new AuthUserRequestDto(userId.toString());
	}


	public static AuthUserRequestDto from(List<Integer> userIds) {
		String userIdsToString = userIds.stream()
			.map(Object::toString)
			.collect(Collectors.joining(","));
		return new AuthUserRequestDto(userIdsToString);
	}
}

