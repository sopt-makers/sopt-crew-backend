package org.sopt.makers.crew.main.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import io.sentry.Sentry;
import jakarta.annotation.PostConstruct;

@Configuration
public class SentryConfig {

	@Value("${sentry.dsn}")
	private String sentryDsn;

	@Value("${sentry.environment}")
	private String environment;

	@Value("${sentry.servername}")
	private String serverName;
	
	@PostConstruct
	public void initSentry() {
		Sentry.init(options -> {
			options.setDsn(sentryDsn);
			options.setEnvironment(environment);
			options.setServerName(serverName);
		});
	}
}
