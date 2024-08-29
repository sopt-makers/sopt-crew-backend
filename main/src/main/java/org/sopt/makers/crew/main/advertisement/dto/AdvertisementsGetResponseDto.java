package org.sopt.makers.crew.main.advertisement.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(name = "AdvertisementGetResponseDto", description = "광고 구좌 조회 응답 Dto")
@Getter
@RequiredArgsConstructor
public class AdvertisementsGetResponseDto {

	@Schema(description = "광고 구좌 이미지 객체", example = "")
	@NotNull
	private final List<AdvertisementGetDto> advertisements;

	public static AdvertisementsGetResponseDto of(List<AdvertisementGetDto> advertisements) {
		return new AdvertisementsGetResponseDto(advertisements);
	}
}
