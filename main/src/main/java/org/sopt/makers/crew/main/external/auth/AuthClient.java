package org.sopt.makers.crew.main.external.auth;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.List;

import java.util.function.Supplier;

import org.sopt.makers.crew.main.external.auth.config.AuthClientProperties;
import org.sopt.makers.crew.main.external.auth.dto.response.AuthApiResponseDto;
import org.sopt.makers.crew.main.external.auth.dto.response.AuthUserResponseDto;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthClient {

	private final WebClient authWebClient;
	private final AuthClientProperties authProperties;

	public AuthUserResponseDto getUserInfo(String userId) {
		return getAuthApiResponseDto(userId).getFirstUser();
	}

	public List<AuthUserResponseDto> getUsers(String userIds) {
		return getAuthApiResponseDto(userIds).data();
	}

	public String getJwk() {
		return executeRequest(
			() -> authWebClient.get()
				.uri(authProperties.getJwk())
				.retrieve()
				.bodyToMono(String.class)
		);
	}

	private AuthApiResponseDto getAuthApiResponseDto(String userId) {
		String fullUrl = authProperties.getUsers() + "?userIds=" + userId;
		log.info("🔍 [DEBUG] Requesting auth server - URL: {}, userId: {}", fullUrl, userId);

		AuthApiResponseDto response = executeRequest(
			() -> authWebClient.get()
				.uri(uriBuilder -> uriBuilder
					.path(authProperties.getUsers())
					.queryParam("userIds", userId)
					.build())
				.retrieve()
				.bodyToMono(AuthApiResponseDto.class)
		);

		log.info("🔍 [DEBUG] Auth server response - success: {}, message: {}, data size: {}",
			response != null ? response.success() : "null",
			response != null ? response.message() : "null",
			response != null && response.data() != null ? response.data().size() : "null");

		validateResponse(response);
		return response;

	}

	private <T> T executeRequest(Supplier<Mono<T>> requestSupplier) {
		try {
			return requestSupplier.get()
				.onErrorMap(WebClientResponseException.class, ex -> {
					log.error("🚨 [ERROR] Failed to receive response from auth server - Status: {}, Body: {}, Headers: {}",
						ex.getStatusCode(), ex.getResponseBodyAsString(), ex.getHeaders(), ex);
					return new ServerException(EXTERNAL_SERVER_RESPONSE_ERROR.getErrorCode());
				})
				.block();
		} catch (RuntimeException e) {
			log.error("🚨 [ERROR] Unexpected exception during auth server communication - Type: {}, Message: {}",
				e.getClass().getSimpleName(), e.getMessage(), e);
			throw new ServerException(EXTERNAL_SERVER_COMMUNICATION_ERROR.getErrorCode());
		}
	}

	private void validateResponse(AuthApiResponseDto response) {
		if (response == null) {
			log.error("Received null response from auth server");
			throw new ServerException(EXTERNAL_SERVER_RESPONSE_ERROR.getErrorCode());
		}

		if (!response.success()) {
			log.error("Auth server returned failure response: {}", response.message());
			throw new ServerException(EXTERNAL_SERVER_RESPONSE_ERROR.getErrorCode());
		}
	}
}
