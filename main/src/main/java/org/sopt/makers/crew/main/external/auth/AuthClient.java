package org.sopt.makers.crew.main.external.auth;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.EXTERNAL_SERVER_COMMUNICATION_ERROR;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.EXTERNAL_SERVER_RESPONSE_ERROR;

import java.util.List;
import java.util.function.Supplier;

import org.sopt.makers.crew.main.external.auth.config.AuthClientProperties;
import org.sopt.makers.crew.main.external.auth.dto.response.AuthApiResponseDto;
import org.sopt.makers.crew.main.external.auth.dto.response.AuthUserResponseDto;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthClient {

	private final RestTemplate authRestTemplate;
	private final AuthClientProperties authProperties;

	public AuthUserResponseDto getUserInfo(String userId) {
		return getAuthApiResponseDto(userId).getFirstUser();
	}

	public List<AuthUserResponseDto> getUsers(String userIds) {
		return getAuthApiResponseDto(userIds).data();
	}

	public String getJwk() {
		return getFromApi(authProperties.getJwk(), String.class);
	}

	private AuthApiResponseDto getAuthApiResponseDto(String userId) {
		String uri = UriComponentsBuilder.fromPath(authProperties.getUsers())
				.queryParam("userIds", userId)
				.toUriString();

		AuthApiResponseDto response = getFromApi(uri, AuthApiResponseDto.class);
		validateResponse(response);
		return response;
	}

	private <T> T getFromApi(String path, Class<T> responseType) {
		return executeRequest(() -> {
			ResponseEntity<T> entity = authRestTemplate.getForEntity(path, responseType);
			T body = entity.getBody();

			if (body == null) {
				log.error("Received null response body from auth server for path: {}", path);
				throw new RestClientException("Received null response body");
			}

			return body;
		});
	}

	private <T> T executeRequest(Supplier<T> requestSupplier) {
		try {
			return requestSupplier.get();
		} catch (RestClientException ex) {
			log.error("Failed to receive response from auth server: {}", ex.getMessage(), ex);
			throw new ServerException(EXTERNAL_SERVER_RESPONSE_ERROR.getErrorCode());
		} catch (Exception e) {
			log.error("Unexpected exception occurred during auth server communication: {}", e.getMessage(), e);
			throw new ServerException(EXTERNAL_SERVER_COMMUNICATION_ERROR.getErrorCode());
		}
	}

	private void validateResponse(AuthApiResponseDto response) {
		if (!response.success()) {
			log.error("Auth server returned failure response: {}", response.message());
			throw new ServerException(EXTERNAL_SERVER_RESPONSE_ERROR.getErrorCode());
		}
	}
}
