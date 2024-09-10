package org.sopt.makers.crew.main.advertisement.dto;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.entity.advertisement.Advertisement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Schema(name = "AdvertisementImageDto", description = "광고 구좌 이미지 Dto")
public class AdvertisementGetDto {

	@Schema(description = "광고 id", example = "3")
	@NotNull
	private final Integer advertisementId;

	@Schema(description = "[Desktop] 광고 구좌 이미지 url", example = "[pc 버전 url 형식]")
	@NotNull
	private final String desktopImageUrl;

	@Schema(description = "[mobile] 광고 구좌 이미지 url", example = "[mobile 버전 url 형식]")
	@NotNull
	private final String mobileImageUrl;

	@Schema(description = "광고 구좌 링크", example = "https://www.naver.com")
	@NotNull
	private final String advertisementLink;

	@Schema(description = "광고 게시 시작일", example = "2024-07-31T00:00:00")
	@NotNull
	private final LocalDateTime advertisementStartDate;

	public static AdvertisementGetDto of(Advertisement advertisement) {
		return new AdvertisementGetDto(advertisement.getId(), advertisement.getAdvertisementDesktopImageUrl(),
			advertisement.getAdvertisementMobileImageUrl(), advertisement.getAdvertisementLink(),
			advertisement.getAdvertisementStartDate());
	}
}
