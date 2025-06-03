package org.sopt.makers.crew.main.external.auth.dto.request;

public record AuthUserRequestDto(
	String userId
) {
	public static AuthUserRequestDto from(Integer userId) {
		return new AuthUserRequestDto(userId.toString());
	}
}
