package org.sopt.makers.crew.main.internal.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserAppliedMeetingDto(
	@Schema(description = "모임 분류, [스터디 or 행사 or 세미나 or 번쩍 or 강연]", example = "스터디")
	String meetingCategory,
	@Schema(description = "모임 제목", example = "오늘 21시 강남 스터디")
	String meetingTitle,
	@Schema(description = "모임 시작 기간")
	LocalDateTime mStartTime,
	@Schema(description = "모임 종료 기간")
	LocalDateTime mEndTime,
	@Schema(description = "스장 여부 (공동 스장도 true입니다)")
	Boolean isLeader,
	@Schema(description = "모임 이미지 url")
	String imgUrl) {

	public static UserAppliedMeetingDto of(
		String meetingCategory, String meetingTitle,
		LocalDateTime mStartTime, LocalDateTime mEndTime, Boolean isLeader, String imgUrl
	) {
		return new UserAppliedMeetingDto(
			meetingCategory, meetingTitle, mStartTime, mEndTime, isLeader, imgUrl
		);
	}
}
