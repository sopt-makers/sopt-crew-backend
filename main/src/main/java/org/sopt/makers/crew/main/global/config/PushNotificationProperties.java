package org.sopt.makers.crew.main.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "push-notification")
@RequiredArgsConstructor
public class PushNotificationProperties {
	private final String webUrl;

	public String getPushWebUrl() {
		return webUrl;
	}
}
