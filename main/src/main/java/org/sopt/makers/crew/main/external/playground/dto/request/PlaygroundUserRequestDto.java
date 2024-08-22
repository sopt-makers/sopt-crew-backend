package org.sopt.makers.crew.main.external.playground.dto.request;


public record PlaygroundUserRequestDto(
	String accessToken

) {
	public static PlaygroundUserRequestDto of(String accessToken){
		return new PlaygroundUserRequestDto(accessToken);
	}
}
