package org.sopt.makers.crew.main.advertisement.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "AdvertisementGetResponseDto", description = "광고 구좌 조회 응답 Dto")
public record AdvertisementGetResponseDto(
	@Schema(description = "광고 구좌 이미지 객체", example = "[image Url]")
	@NotNull
	List<AdvertisementImageDto> advertisementImages,
	@Schema(description = "광고 구좌 링크", example = "https://www.naver.com")
	@NotNull
	String advertisementLink
) {
	public static AdvertisementGetResponseDto of(List<AdvertisementImageDto> advertisementImages,
		String advertisementLink) {

		return new AdvertisementGetResponseDto(advertisementImages, advertisementLink);
	}
}
