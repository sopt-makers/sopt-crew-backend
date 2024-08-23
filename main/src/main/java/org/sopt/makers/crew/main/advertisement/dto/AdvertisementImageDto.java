package org.sopt.makers.crew.main.advertisement.dto;

import org.sopt.makers.crew.main.entity.advertisement.AdvertisementImage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Schema(name = "AdvertisementImageDto", description = "광고 구좌 이미지 Dto")
public class AdvertisementImageDto {

	@Schema(description = "광고 구좌 이미지 url", example = "[url 형식]")
	@NotNull
	private final String imageUrl;

	@Schema(description = "광고 이미지 순서, 서버에서 정렬해서 전달한다. </br> 프론트에서 확인용으로 사용하기 위함", example = "2")
	@NotNull
	private final int imageOrder;

	public static AdvertisementImageDto of(AdvertisementImage advertisementImage){
		return new AdvertisementImageDto(advertisementImage.getAdvertisementImageUrl(), advertisementImage.getImageOrder());
	}
}
