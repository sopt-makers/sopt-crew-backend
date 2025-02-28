package org.sopt.makers.crew.main.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "img")
@RequiredArgsConstructor
public class ImageSettingProperties {
	private final String flash;

	public String getDefaultFlashImage() {
		return flash;
	}
}
