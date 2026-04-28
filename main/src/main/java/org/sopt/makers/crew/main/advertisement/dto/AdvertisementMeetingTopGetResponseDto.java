package org.sopt.makers.crew.main.advertisement.dto;

import org.sopt.makers.crew.main.entity.advertisement.Advertisement;
import org.sopt.makers.crew.main.entity.advertisement.enums.EventType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "AdvertisementMeetingTopGetResponseDto", description = "모임 상단 광고 조회 응답 Dto")
public record AdvertisementMeetingTopGetResponseDto(
	@Schema(description = "배너 노출 여부", example = "true")
	@NotNull
	Boolean isDisplay,

	@Schema(description = "배너 이벤트 타입", example = "SOPKATHON")
	EventType eventType,

	@Schema(description = "배너 id", example = "3")
	Integer advertisementId,

	@Schema(description = "[Desktop] 배너 이미지 url", example = "[pc 버전 url 형식]")
	String desktopImageUrl,

	@Schema(description = "[Mobile] 배너 이미지 url", example = "[mobile 버전 url 형식]")
	String mobileImageUrl,

	@Schema(description = "달력 이미지 url", example = "[달력 이미지 url 형식]")
	String calendarImageUrl,

	@Schema(description = "배너 제목")
	Title title,

	@Schema(description = "배너 부제목", example = "우리만의 해커톤, 누구보다 빠르게 신청하세요!")
	String subTitle,

	@Schema(description = "배너 링크 1", example = "")
	String bannerLink1,

	@Schema(description = "배너 링크 2", example = "")
	String bannerLink2
) {

	@Schema(name = "AdvertisementMeetingTopTitleDto", description = "모임 상단 광고 제목 Dto")
	public record Title(
		@Schema(description = "배너 제목 prefix", example = "5월 4일 ")
		String prefix,

		@Schema(description = "배너 제목 highlight", example = "솝커톤 신청")
		String highlight,

		@Schema(description = "배너 제목 suffix", example = " OPEN!")
		String suffix
	) {
		public static Title of(Advertisement advertisement) {
			return new Title(
				advertisement.getTitlePrefix(),
				advertisement.getTitleHighlight(),
				advertisement.getTitleSuffix()
			);
		}
	}

	public static AdvertisementMeetingTopGetResponseDto of(Advertisement advertisement, Integer activeGeneration,
		String part) {
		return new AdvertisementMeetingTopGetResponseDto(
			advertisement.isDisplay(),
			advertisement.getEventType(),
			advertisement.getId(),
			advertisement.getAdvertisementDesktopImageUrl(),
			advertisement.getAdvertisementMobileImageUrl(),
			advertisement.getCalendarImageUrl(),
			Title.of(advertisement),
			advertisement.getSubTitle(),
			createBannerLink1(activeGeneration, advertisement.getEventType()),
			createBannerLink2(part, advertisement.getEventType())
		);
	}

	public static AdvertisementMeetingTopGetResponseDto notDisplay() {
		return new AdvertisementMeetingTopGetResponseDto(
			false,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null
		);
	}

	private static final int FIXED_PAGE = 1;
	private static final String MEETING_LIST_PATH = "/list";

	private static String createBannerLink1(Integer activeGeneration, EventType eventType) {
		return String.format("%s?search=%d기+%s&page=%d", MEETING_LIST_PATH, activeGeneration,
			eventType.getDisplayName(), FIXED_PAGE);
	}

	private static String createBannerLink2(String part, EventType eventType) {
		return String.format("%s?page=%d&part=%s&search=%s", MEETING_LIST_PATH, FIXED_PAGE, part,
			eventType.getDisplayName());
	}
}
