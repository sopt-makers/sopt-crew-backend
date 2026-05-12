package org.sopt.makers.crew.main.admin.v2.dto;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.entity.advertisement.Advertisement;

public record AdvertisementMeetingTopUpdateResponse(
	Integer advertisementId,
	Boolean isDisplay,
	LocalDateTime advertisementStartDate,
	LocalDateTime advertisementEndDate,
	String desktopImageUrl,
	String mobileImageUrl,
	String calendarImageUrl,
	String titlePrefix,
	String titleHighlight,
	String titleSuffix,
	String subTitle
) {
	public static AdvertisementMeetingTopUpdateResponse from(Advertisement advertisement) {
		return new AdvertisementMeetingTopUpdateResponse(
			advertisement.getId(),
			advertisement.isDisplay(),
			advertisement.getAdvertisementStartDate(),
			advertisement.getAdvertisementEndDate(),
			advertisement.getAdvertisementDesktopImageUrl(),
			advertisement.getAdvertisementMobileImageUrl(),
			advertisement.getCalendarImageUrl(),
			advertisement.getTitlePrefix(),
			advertisement.getTitleHighlight(),
			advertisement.getTitleSuffix(),
			advertisement.getSubTitle()
		);
	}
}
