package org.sopt.makers.crew.main.external.auth.dto.request;

public record PlaygroundUserRequestDto(
	String accessToken
) {
	public static PlaygroundUserRequestDto from(String accessToken) {
		return new PlaygroundUserRequestDto(accessToken);
	}
}
