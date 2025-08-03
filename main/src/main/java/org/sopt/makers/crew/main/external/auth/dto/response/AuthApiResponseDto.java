package org.sopt.makers.crew.main.external.auth.dto.response;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.List;

import org.sopt.makers.crew.main.global.exception.ServerException;

public record AuthApiResponseDto(
	boolean success,
	String message,
	List<AuthUserResponseDto> data
) {
	public AuthUserResponseDto getFirstUser() {
		if (data == null || data.isEmpty()) {
			throw new ServerException(EXTERNAL_SERVER_RESPONSE_ERROR.getErrorCode());
		}

		return data.get(0);
	}

	public List<AuthUserResponseDto> getUsers() {
		if (data == null || data.isEmpty()) {
			throw new ServerException(EXTERNAL_SERVER_RESPONSE_ERROR.getErrorCode());
		}

		return data;
	}

}
