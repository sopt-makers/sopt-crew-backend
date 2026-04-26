package org.sopt.makers.crew.main.admin.v2.dto;

import org.sopt.makers.crew.main.entity.advertisement.Advertisement;

public record AdvertisementDisplayUpdateResponse(Integer advertisementId, Boolean isDisplay) {
	public static AdvertisementDisplayUpdateResponse from(Advertisement advertisement) {
		return new AdvertisementDisplayUpdateResponse(advertisement.getId(), advertisement.isDisplay());
	}
}
