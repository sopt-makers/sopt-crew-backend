package org.sopt.makers.crew.main.external.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "external.auth")
@Getter
@Setter
@Component
public class AuthClientProperties {
	private String url;
	private String apiKey;
	private String serviceName;
	private String jwk;
	private String users;

}
