package org.sopt.makers.crew.main.advertisement.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.crew.main.entity.advertisement.Advertisement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "AdvertisementsGetResponseDto", description = "광고 구좌 조회 응답 Dto")
public record AdvertisementsGetResponseDto(
	@Schema(description = "광고 구좌 이미지 리스트", example = "")
	@NotNull
	List<AdvertisementGetDto> advertisements
) {
	@Schema(name = "AdvertisementGetDto", description = "광고 구좌 이미지 Dto")
	public record AdvertisementGetDto(

		@Schema(description = "광고 id", example = "3")
		@NotNull
		Integer advertisementId,

		@Schema(description = "[Desktop] 광고 구좌 이미지 url", example = "[pc 버전 url 형식]")
		@NotNull
		String desktopImageUrl,

		@Schema(description = "[mobile] 광고 구좌 이미지 url", example = "[mobile 버전 url 형식]")
		@NotNull
		String mobileImageUrl,

		@Schema(description = "광고 구좌 링크", example = "https://www.naver.com")
		@NotNull
		String advertisementLink,

		@Schema(description = "광고 게시 시작일", example = "2024-07-31T00:00:00")
		@NotNull
		LocalDateTime advertisementStartDate
	) {
		public static AdvertisementGetDto of(Advertisement advertisement) {
			return new AdvertisementGetDto(
				advertisement.getId(),
				advertisement.getAdvertisementDesktopImageUrl(),
				advertisement.getAdvertisementMobileImageUrl(),
				advertisement.getAdvertisementLink(),
				advertisement.getAdvertisementStartDate()
			);
		}
	}

	public static AdvertisementsGetResponseDto of(List<AdvertisementGetDto> advertisements) {
		return new AdvertisementsGetResponseDto(advertisements);
	}
}