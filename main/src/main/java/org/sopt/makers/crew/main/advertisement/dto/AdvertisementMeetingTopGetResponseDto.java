package org.sopt.makers.crew.main.advertisement.dto;

import java.time.LocalDateTime;

import org.sopt.makers.crew.main.entity.advertisement.Advertisement;
import org.sopt.makers.crew.main.entity.advertisement.enums.EventType;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.vo.MeetingJoinInfo;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ParticipatingPartInfoDto;

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

	@Schema(description = "배너 링크", example = "https://www.naver.com")
	String advertisementLink,

	@Schema(description = "조회 기준 기수", example = "38")
	Integer generation,

	@Schema(description = "조회 기준 파트", example = "서버")
	String part,

	@Schema(description = "신청 모임 id", example = "123")
	Integer meetingId,

	@Schema(description = "신청 모임 제목", example = "[38기 솝커톤] 서버 파트 신청")
	String title,

	@Schema(description = "신청 모임 부제목", example = "모임 부제목")
	String subTitle,

	@Schema(description = "모임 신청 시작 시간", example = "2026-05-02T00:00:00")
	LocalDateTime startDate,

	@Schema(description = "모임 신청 종료 시간", example = "2026-05-02T23:59:59")
	LocalDateTime endDate,

	@Schema(description = "모임 상태, 0: 모집전, 1: 모집중, 2: 모집종료", example = "1")
	Integer status,

	@Schema(description = "참여 정보")
	MeetingJoinInfo joinInfo,

	@Schema(description = "조회자와 같은 파트 참여 정보")
	MeetingV2ParticipatingPartInfoDto participatingPartInfo,

	@Schema(description = "파트별 모임 둘러보기 액션")
	BrowseActionDto browseAction
) {
	public static AdvertisementMeetingTopGetResponseDto of(Advertisement advertisement, Meeting meeting,
		UserActivityVO userActivity, MeetingV2ParticipatingPartInfoDto participatingPartInfo, String browseQuery,
		LocalDateTime now) {
		return new AdvertisementMeetingTopGetResponseDto(
			true,
			advertisement.getEventType(),
			advertisement.getId(),
			advertisement.getAdvertisementDesktopImageUrl(),
			advertisement.getAdvertisementMobileImageUrl(),
			advertisement.getAdvertisementLink(),
			userActivity.getGeneration(),
			userActivity.getPart(),
			meeting.getId(),
			meeting.getTitle(),
			meeting.getSubTitle(),
			meeting.getStartDate(),
			meeting.getEndDate(),
			meeting.getMeetingStatusValue(now),
			meeting.getJoinInfo(),
			participatingPartInfo,
			BrowseActionDto.of(browseQuery)
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

	@Schema(name = "AdvertisementMeetingTopBrowseActionDto", description = "파트별 모임 둘러보기 Dto")
	public record BrowseActionDto(
		@Schema(description = "기존 모임 목록 조회 query", example = "38기 솝커톤")
		@NotNull
		String query,

		@Schema(description = "기존 모임 목록 조회 page", example = "1")
		@NotNull
		Integer page
	) {
		private static final int FIRST_PAGE = 1;

		public static BrowseActionDto of(String query) {
			return new BrowseActionDto(query, FIRST_PAGE);
		}
	}
}
