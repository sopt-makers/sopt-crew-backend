package org.sopt.makers.crew.main.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImageSettingConfig implements ImageSetting {
	@Value("${img.lightning}")
	private String defaultLightningImage;

	@Override
	public String getDefaultLightningImage() {
		return defaultLightningImage;
	}
}
