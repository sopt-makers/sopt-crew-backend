package org.sopt.makers.crew.main.admin.v2.dto;

public record AdvertisementImageUploadResponse(String publicUrl) {
	public static AdvertisementImageUploadResponse of(String publicUrl) {
		return new AdvertisementImageUploadResponse(publicUrl);
	}
}
