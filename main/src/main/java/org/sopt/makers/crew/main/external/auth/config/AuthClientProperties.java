package org.sopt.makers.crew.main.external.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "external.client")
@Getter
@RequiredArgsConstructor
public class AuthClientProperties {
	private final String url;
	private final String apiKey;
	private final String serviceName;
	private final Endpoints endpoints;

	@Getter
	@RequiredArgsConstructor
	public static class Endpoints {
		private final String jwk;
		private final String users;
	}
}
