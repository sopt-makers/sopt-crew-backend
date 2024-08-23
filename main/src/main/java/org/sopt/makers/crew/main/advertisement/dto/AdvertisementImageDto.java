package org.sopt.makers.crew.main.advertisement.dto;

import org.sopt.makers.crew.main.entity.advertisement.AdvertisementImage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Schema(name = "AdvertisementImageDto", description = "광고 구좌 이미지 Dto")
public class AdvertisementImageDto {
	private final String imageUrl;
	private final int order;

	public static AdvertisementImageDto of(AdvertisementImage advertisementImage){
		return new AdvertisementImageDto(advertisementImage.getAdvertisementImageUrl(), advertisementImage.getImageOrder());
	}
}
