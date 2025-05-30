package org.sopt.makers.crew.main.external.auth;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import org.sopt.makers.crew.main.external.auth.config.AuthClientProperties;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthClient {
	private final WebClient authWebClient;
	private final AuthClientProperties authProperties;

	public String getJwk() {
		try {
			return authWebClient.get()
				.uri(authProperties.endpoints().jwk())
				.retrieve()
				.bodyToMono(String.class)
				.onErrorMap(WebClientResponseException.class, ex -> {
					log.error("Failed to receive response from Auth server: {}", ex.getResponseBodyAsString(), ex);
					return new ServerException(EXTERNAL_SERVER_RESPONSE_ERROR.getErrorCode());
				})
				.block();
		} catch (RuntimeException e) {
			log.error("Unexpected exception occurred during Auth server communication: {}", e.getMessage(), e);
			throw new ServerException(EXTERNAL_SERVER_COMMUNICATION_ERROR.getErrorCode());
		}
	}
}
