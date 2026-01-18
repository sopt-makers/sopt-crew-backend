package org.sopt.makers.crew.main.external.auth.config;

import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AuthRestTemplateConfig {
    public static final String HEADER_API_KEY = "X-Api-Key";
    public static final String HEADER_SERVICE_NAME = "X-Service-Name";
    private static final int TIMEOUT_SECONDS = 5;

    @Bean
    public RestTemplate authRestTemplate(
            RestTemplateBuilder builder,
            AuthClientProperties properties) {
        return builder
                .rootUri(properties.getUrl())
                .setConnectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .setReadTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .defaultHeader(HEADER_API_KEY, properties.getApiKey())
                .defaultHeader(HEADER_SERVICE_NAME, properties.getServiceName())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
