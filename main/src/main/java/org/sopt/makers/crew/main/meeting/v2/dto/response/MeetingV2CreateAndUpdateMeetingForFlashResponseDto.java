package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.flash.v2.dto.request.FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "MeetingV2CreateAndUpdateMeetingForFlashResponseDto", description = "번쩍 모임 생성 및 수정 request body dto")
public record MeetingV2CreateAndUpdateMeetingForFlashResponseDto(
	@Schema(description = "모임 id", example = "1")
	@NotNull
	Integer meetingId,

	@Schema(example = "알고보면 쓸데있는 개발 프로세스", description = "번쩍 모임 제목")
	@Size(min = 1, max = 30)
	@NotNull String title,

	@Schema(example = "api 가 터졌다고? 깃이 터졌다고?", description = "번쩍 소개")
	@Size(min = 1, max = 500)
	@NotNull
	String desc,

	@Schema(example = "예정 기간 (협의 후 결정)", description = "번쩍 일정 결정 방식")
	@NotNull
	String flashTimingType,

	@Schema(example = "2025.10.29", description = "번쩍 활동 시작 날짜", name = "activityStartDate")
	@NotNull
	String activityStartDate,

	@Schema(example = "2025.10.30", description = "번쩍 활동 종료 날짜", name = "activityEndDate")
	@NotNull
	String activityEndDate,

	@Schema(example = "오프라인", description = "모임 장소 Tag")
	@NotNull
	String flashPlaceType,

	@Schema(example = "잠실역 5번 출구", description = "모임 장소")
	String flashPlace,

	@Schema(example = "1", description = "최소 모집 인원")
	@Min(1)
	@NotNull
	Integer minimumCapacity,

	@Schema(example = "5", description = "최대 모집 인원")
	@Min(1)
	@Max(999)
	@NotNull
	Integer maximumCapacity,

	@Schema(example = "[\n"
		+ "    \"https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2023/04/12/7bd87736-b557-4b26-a0d5-9b09f1f1d7df\"\n"
		+ "  ]", description = "모임 이미지 리스트, 최대 1개")
	@NotNull
	@Size(max = 1)
	List<String> files
) {
	public static MeetingV2CreateAndUpdateMeetingForFlashResponseDto of(
		Integer meetingId, FlashV2CreateAndUpdateFlashBodyWithoutWelcomeMessageDto flashBody) {
		return new MeetingV2CreateAndUpdateMeetingForFlashResponseDto(
			meetingId,
			flashBody.title(),
			flashBody.desc(),
			flashBody.flashTimingType(),
			flashBody.activityStartDate(),
			flashBody.activityEndDate(),
			flashBody.flashPlaceType(),
			flashBody.flashPlace(),
			flashBody.minimumCapacity(),
			flashBody.maximumCapacity(),
			flashBody.files()
		);
	}
}
