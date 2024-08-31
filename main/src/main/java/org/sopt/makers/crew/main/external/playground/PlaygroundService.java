package org.sopt.makers.crew.main.external.playground;

import java.util.HashMap;
import java.util.Map;

import org.sopt.makers.crew.main.external.playground.dto.request.PlaygroundUserRequestDto;
import org.sopt.makers.crew.main.external.playground.dto.response.PlaygroundUserResponseDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaygroundService {
	private final PlaygroundServer playgroundServer;

	public PlaygroundUserResponseDto getUser(PlaygroundUserRequestDto requestDto){
		return playgroundServer.getUser(requestDto.accessToken());
	}


	private Map<String, String> createAuthorizationHeader(String accessToken) {
		Map<String, String> headers = createDefaultHeader();
		headers.put(HttpHeaders.AUTHORIZATION, accessToken);
		return headers;
	}

	private Map<String, String> createDefaultHeader() {
		return new HashMap<>(Map.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
	}
}
