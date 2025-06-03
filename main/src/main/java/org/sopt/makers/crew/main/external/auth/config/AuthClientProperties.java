package org.sopt.makers.crew.main.external.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.client")
public record AuthClientProperties(
	String url,
	String apiKey,
	String serviceName,
	Endpoints endpoints
) {
	public record Endpoints(
		String jwk,
		String users
	) {
	}
}
