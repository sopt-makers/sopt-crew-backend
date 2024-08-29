package org.sopt.makers.crew.main.advertisement.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "AdvertisementGetResponseDto", description = "광고 구좌 조회 응답 Dto")
public record AdvertisementsGetResponseDto(
	@Schema(description = "광고 구좌 이미지 객체", example = "")
	@NotNull
	List<AdvertisementGetDto> advertisementImages
) {
	public static AdvertisementsGetResponseDto of(List<AdvertisementGetDto> advertisementImages) {

		return new AdvertisementsGetResponseDto(advertisementImages);
	}
}
