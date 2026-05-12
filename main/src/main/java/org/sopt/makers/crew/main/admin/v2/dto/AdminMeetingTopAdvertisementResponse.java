package org.sopt.makers.crew.main.admin.v2.dto;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sopt.makers.crew.main.entity.advertisement.Advertisement;
import org.sopt.makers.crew.main.entity.advertisement.enums.EventType;
import org.sopt.makers.crew.main.entity.advertisement.enums.TargetGeneration;

public record AdminMeetingTopAdvertisementResponse(
	Integer advertisementId,
	Boolean display,
	EventType eventType,
	TargetGeneration targetGeneration,
	String title,
	String titlePrefix,
	String titleHighlight,
	String titleSuffix,
	String subTitle,
	String desktopImageUrl,
	String mobileImageUrl,
	String calendarImageUrl,
	String advertisementStartDateTime,
	String advertisementEndDateTime,
	Long priority
) {
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

	public static AdminMeetingTopAdvertisementResponse from(Advertisement advertisement) {
		return new AdminMeetingTopAdvertisementResponse(
			advertisement.getId(),
			advertisement.isDisplay(),
			advertisement.getEventType(),
			advertisement.getTargetGeneration(),
			createTitle(advertisement),
			advertisement.getTitlePrefix(),
			advertisement.getTitleHighlight(),
			advertisement.getTitleSuffix(),
			advertisement.getSubTitle(),
			advertisement.getAdvertisementDesktopImageUrl(),
			advertisement.getAdvertisementMobileImageUrl(),
			advertisement.getCalendarImageUrl(),
			advertisement.getAdvertisementStartDate().format(DATE_TIME_FORMATTER),
			advertisement.getAdvertisementEndDate().format(DATE_TIME_FORMATTER),
			advertisement.getPriority()
		);
	}

	private static String createTitle(Advertisement advertisement) {
		return Stream.of(
				advertisement.getTitlePrefix(),
				advertisement.getTitleHighlight(),
				advertisement.getTitleSuffix())
			.filter(Objects::nonNull)
			.collect(Collectors.joining());
	}
}
