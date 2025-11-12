package org.sopt.makers.crew.main.external.auth.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;

@Configuration
@Slf4j
public class AuthWebClientConfig {
	public static final String HEADER_API_KEY = "X-Api-Key";
	public static final String HEADER_SERVICE_NAME = "X-Service-Name";
	private static final int TIMEOUT_MILLIS = 5000;
	private static final int TIMEOUT_SECONDS = 5;

	@Bean
	public WebClient authWebClient(AuthClientProperties properties) {
		log.info("🔍 [DEBUG] AuthWebClient Config - URL: {}, Service: {}, API Key: {}",
			properties.getUrl(),
			properties.getServiceName(),
			(properties.getApiKey() != null
				? "***" + properties.getApiKey().substring(Math.max(0, properties.getApiKey().length() - 4))
				: "null"));

		return WebClient.builder()
			.baseUrl(properties.getUrl())
			.clientConnector(new ReactorClientHttpConnector(createDefaultHttpClient()))
			.defaultHeader(HEADER_API_KEY, properties.getApiKey())
			.defaultHeader(HEADER_SERVICE_NAME, properties.getServiceName())
			.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	private HttpClient createDefaultHttpClient() {
		return HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT_MILLIS)
			.responseTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
			.doOnConnected(conn -> conn
				.addHandlerLast(new ReadTimeoutHandler(TIMEOUT_SECONDS))
				.addHandlerLast(new WriteTimeoutHandler(TIMEOUT_SECONDS))
			);
	}
}
