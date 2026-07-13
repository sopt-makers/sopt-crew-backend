package org.sopt.makers.crew.main.meetingdemand.v2.dto.response;

import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MeetingDemandV2GetMeetingDemandSummaryResponseDto", description = "모임 수요 목록 아이템 응답 Dto")
public record MeetingDemandV2GetMeetingDemandSummaryResponseDto(
	@Schema(description = "모임 수요 id", example = "1")
	@NotNull
	Integer id,

	@Schema(description = "모임 한줄소개", example = "러닝 모임 열어주세요")
	@NotNull
	String shortIntro,

	@Schema(description = "기대하는 내용", example = "함께 꾸준히 달릴 수 있는 모임이 있으면 좋겠어요.")
	@NotNull
	String expectation,

	@Schema(description = "모임 수요 상태", example = "BEFORE_OPEN")
	@NotNull
	String status,

	@Schema(description = "본인이 작성한 모임 수요인지 여부", example = "true")
	@NotNull
	Boolean isMine,

	@Schema(description = "기다려요 수", example = "10")
	@NotNull
	int waitCount,

	@Schema(description = "본인이 기다려요를 눌렀는지 여부", example = "true")
	@NotNull
	Boolean isWaiting
) {
	public static MeetingDemandV2GetMeetingDemandSummaryResponseDto of(MeetingDemand meetingDemand,
		boolean isMine, boolean isWaiting) {
		return new MeetingDemandV2GetMeetingDemandSummaryResponseDto(
			meetingDemand.getId(),
			meetingDemand.getShortIntro(),
			meetingDemand.getExpectation(),
			meetingDemand.getStatus().name(),
			isMine,
			meetingDemand.getWaitCount(),
			isWaiting
		);
	}
}
