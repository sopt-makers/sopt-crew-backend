package org.sopt.makers.crew.main.advertisement.dto;

import org.sopt.makers.crew.main.entity.advertisement.Advertisement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "AdvertisementGetResponseDto", description = "광고 구좌 조회 응답 Dto")
public record AdvertisementGetResponseDto(
	@Schema(description = "광고 구좌 이미지 url", example = "[image Url]")
	@NotNull
	String advertisementImageUrl,
	@Schema(description = "광고 구좌 링크", example = "https://www.naver.com")
	@NotNull
	String advertisementLink
) {
	public static AdvertisementGetResponseDto of(Advertisement advertisement){
		if(advertisement == null){
			return new AdvertisementGetResponseDto(null, null);
		}

		return new AdvertisementGetResponseDto(advertisement.getAdvertisementImageUrl(),
			advertisement.getAdvertisementLink());
	}
}
