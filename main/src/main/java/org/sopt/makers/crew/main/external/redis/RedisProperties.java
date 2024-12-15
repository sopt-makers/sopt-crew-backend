package org.sopt.makers.crew.main.external.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@ConfigurationProperties(prefix = "spring.data.redis")
@RequiredArgsConstructor
public class RedisProperties {
	private final String host;
	private final int port;
}
