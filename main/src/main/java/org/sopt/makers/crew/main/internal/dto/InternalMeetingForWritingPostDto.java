package org.sopt.makers.crew.main.internal.dto;

import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InternalMeetingForWritingPostDto", description = "[Internal] 모임 피드 작성시 모임 전체 조회 응답 Dto")
public record InternalMeetingForWritingPostDto(
	@Schema(description = "모임 제목", example = "오늘 21시 강남 스터디")
	String title,
	@Schema(description = "모임 분류, [스터디 or 행사 or 세미나 or 번쩍 or 강연]", example = "스터디", allowableValues = {"스터디", "행사",
		"세미나", " 번쩍", " 강연"})
	MeetingCategory category,
	@Schema(description = "모임 이미지", example = "[url 형식]")
	String imageUrl,
	@Schema(description = "모임 설명", example = "해당 모임은 이런 모임이에요~~")
	String description) {

	public static InternalMeetingForWritingPostDto from(Meeting entity) {
		return new InternalMeetingForWritingPostDto(entity.getTitle(),
			entity.getCategory(),
			entity.getImageURL().get(0).getUrl(),
			entity.getDesc());
	}
}
