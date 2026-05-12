package org.sopt.makers.crew.main.admin.v2.dto;

import java.time.LocalDateTime;

public record AdvertisementMeetingTopUpdateRequest(
	Boolean isDisplay,
	LocalDateTime advertisementStartDate,
	LocalDateTime advertisementEndDate,
	String desktopImageUrl,
	String mobileImageUrl,
	String calendarImageUrl
) {
	public static AdvertisementMeetingTopUpdateRequest display(Boolean isDisplay) {
		return new AdvertisementMeetingTopUpdateRequest(isDisplay, null, null, null, null, null);
	}

	public boolean hasUpdateField() {
		return isDisplay != null
			|| advertisementStartDate != null
			|| advertisementEndDate != null
			|| desktopImageUrl != null
			|| mobileImageUrl != null
			|| calendarImageUrl != null;
	}
}
