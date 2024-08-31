package org.sopt.makers.crew.main.advertisement.dto;

import org.sopt.makers.crew.main.entity.advertisement.Advertisement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Schema(name = "AdvertisementImageDto", description = "광고 구좌 이미지 Dto")
public class AdvertisementGetDto {

	@Schema(description = "[Desktop] 광고 구좌 이미지 url", example = "[pc 버전 url 형식]")
	@NotNull
	private final String desktopImageUrl;

	@Schema(description = "[mobile] 광고 구좌 이미지 url", example = "[mobile 버전 url 형식]")
	@NotNull
	private final String mobileImageUrl;

	@Schema(description = "광고 구좌 링크", example = "https://www.naver.com")
	@NotNull
	private final String advertisementLink;

	public static AdvertisementGetDto of(Advertisement advertisement) {
		return new AdvertisementGetDto(advertisement.getAdvertisementDesktopImageUrl(),
			advertisement.getAdvertisementMobileImageUrl(), advertisement.getAdvertisementLink());
	}
}
