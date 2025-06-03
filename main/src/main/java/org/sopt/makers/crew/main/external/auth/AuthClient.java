package org.sopt.makers.crew.main.external.auth;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

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
		AuthApiResponseDto response = executeRequest(
			() -> authWebClient.get()
				.uri(uriBuilder -> uriBuilder
					.path(authProperties.endpoints().users())
					.queryParam("userIds", userId)
					.build())
				.retrieve()
				.bodyToMono(AuthApiResponseDto.class)
		);

		validateResponse(response);
		return response.getFirstUser();
	}

	public String getJwk() {
		return executeRequest(
			() -> authWebClient.get()
				.uri(authProperties.endpoints().jwk())
				.retrieve()
				.bodyToMono(String.class)
		);
	}

	private <T> T executeRequest(Supplier<Mono<T>> requestSupplier) {
		try {
			return requestSupplier.get()
				.onErrorMap(WebClientResponseException.class, ex -> {
					log.error("Failed to receive response from auth server: {}",
						ex.getResponseBodyAsString(), ex);
					return new ServerException(EXTERNAL_SERVER_RESPONSE_ERROR.getErrorCode());
				})
				.block();
		} catch (RuntimeException e) {
			log.error("Unexpected exception occurred during auth server communication: {}",
				e.getMessage(), e);
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
