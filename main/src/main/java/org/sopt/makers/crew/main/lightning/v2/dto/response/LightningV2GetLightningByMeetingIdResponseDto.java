package org.sopt.makers.crew.main.lightning.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.crew.main.entity.lightning.Lightning;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.global.dto.MeetingCreatorDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyWholeInfoDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "번쩍 상세 조회 dto")
public record LightningV2GetLightningByMeetingIdResponseDto(

	@Schema(description = "모임 id", example = "2")
	@NotNull
	Integer id,

	@Schema(description = "번쩍장 id", example = "184")
	@NotNull
	Integer leaderUserId,

	@Schema(description = "번쩍 제목", example = "번쩍 제목입니다.")
	@NotNull
	@Size(min = 1, max = 30)
	String title,

	@Schema(description = "모임 카테고리(번쩍)", example = "번쩍")
	@NotNull
	String category,

	@Schema(description = "번쩍 이미지", example = "[url 형식]")
	@NotNull
	List<ImageUrlVO> imageURL,

	@Schema(description = "번쩍 신청 종료 시간", example = "2025-07-30T23:59:59")
	@NotNull
	LocalDateTime endDate,

	@Schema(example = "1", description = "최소 모집 인원")
	@Min(1)
	@NotNull
	Integer minimumCapacity,

	@Schema(example = "5", description = "최대 모집 인원")
	@Min(1)
	@Max(999)
	@NotNull
	Integer maximumCapacity,

	@Schema(description = "환영 메시지 타입 목록")
	@NotNull
	List<String> welcomeMessageTypes,

	@Schema(description = "번쩍 소개", example = "번쩍 소개 입니다.")
	@NotNull
	String desc,

	@Schema(description = "번쩍 활동 시작 시간", example = "2024-08-13T15:30:00", name = "activityStartDate")
	@NotNull
	LocalDateTime activityStartDate,

	@Schema(description = "번쩍 활동 종료 시간", example = "2024-10-13T23:59:59", name = "activityEndDate")
	@NotNull
	LocalDateTime activityEndDate,

	@Schema(description = "번쩍 일시 타입", example = "예정 기간(협의 후 결정)")
	@NotNull
	String timingType,

	@Schema(description = "번쩍 장소 타입", example = "온라인")
	@NotNull
	String placeType,

	@Schema(description = "번쩍 장소", example = "Zoom 링크")
	@NotNull
	String place,

	@Schema(description = "개설 기수", example = "36")
	@NotNull
	Integer createdGeneration,

	@Schema(description = "번쩍 상태, 0: 모집전, 1: 모집중, 2: 모집종료", example = "1", type = "integer", allowableValues = {"0",
		"1", "2"})
	@NotNull
	int status,

	@Schema(description = "승인된 신청 수", example = "7")
	@NotNull
	long approvedApplyCount,

	@Schema(description = "번쩍 개설자 여부", example = "true")
	@NotNull
	boolean host,

	@Schema(description = "번쩍 신청 여부", example = "false")
	@NotNull
	boolean apply,

	@Schema(description = "번쩍 승인 여부", example = "false")
	@NotNull
	boolean approved,

	@Schema(description = "번쩍장 객체", example = "")
	@NotNull
	MeetingCreatorDto user,

	@Schema(description = "신청 목록", example = "")
	@NotNull
	List<ApplyWholeInfoDto> appliedInfo

) {
	public static LightningV2GetLightningByMeetingIdResponseDto of(
		Integer meetingId,
		Lightning lightning,
		List<WelcomeMessageType> welcomeMessageTypes,
		long approvedCount,
		boolean isHost,
		boolean isApply,
		boolean isApproved,
		MeetingCreatorDto meetingCreatorDto,
		List<ApplyWholeInfoDto> appliedInfo,
		LocalDateTime now
	) {

		int lightningMeetingStatus = lightning.getLightningMeetingStatusValue(now);
		List<String> welcomeMessageTypeValues = welcomeMessageTypes.stream()
			.map(WelcomeMessageType::getValue)
			.toList();

		return new LightningV2GetLightningByMeetingIdResponseDto(
			meetingId,
			lightning.getLeaderUserId(),
			lightning.getTitle(),
			MeetingCategory.LIGHTNING.getValue(),
			lightning.getImageURL(),
			lightning.getEndDate(),
			lightning.getMinimumCapacity(),
			lightning.getMaximumCapacity(),
			welcomeMessageTypeValues,
			lightning.getDesc(),
			lightning.getActivityStartDate(),
			lightning.getActivityEndDate(),
			lightning.getLightningTimingType().getValue(),
			lightning.getLightningPlaceType().getValue(),
			lightning.getLightningPlace(),
			lightning.getCreatedGeneration(),
			lightningMeetingStatus,
			approvedCount,
			isHost,
			isApply,
			isApproved,
			meetingCreatorDto,
			appliedInfo);
	}
}
