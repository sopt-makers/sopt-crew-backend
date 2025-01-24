package org.sopt.makers.crew.main.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImageSettingConfig implements ImageSetting {
	@Value("${img.flash}")
	private String defaultFlashImage;

	@Override
	public String getDefaultFlashImage() {
		return defaultFlashImage;
	}
}
