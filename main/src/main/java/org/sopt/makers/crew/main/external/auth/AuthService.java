package org.sopt.makers.crew.main.external.auth;

import org.sopt.makers.crew.main.external.auth.dto.request.AuthUserRequestDto;
import org.sopt.makers.crew.main.external.auth.dto.response.AuthUserResponseDto;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final AuthClient authClient;

	public AuthUserResponseDto getAuthUser(AuthUserRequestDto requestDto) {
		return authClient.getUserInfo(requestDto.userId());
	}
}
