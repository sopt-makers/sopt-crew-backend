package org.sopt.makers.crew.main.internal.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.EnMeetingStatus;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "InternalMeetingResponseDto", description = "[Internal] 모임 조회 응답 Dto")
public record InternalMeetingResponseDto(
	@Schema(description = "모임 객체 목록", example = "")
	Integer id,
	@Schema(description = "모임 제목", example = "")
	String title,
	@Schema(description = "활동 기수만 신청가능한 여부", example = "false")
	boolean canJoinOnlyActiveGeneration,
	@Schema(
		description = "모임 상태, BEFORE_START: 모집전, APPLY_ABLE: 모집중, RECRUITMENT_COMPLETE: 모집종료",
		example = "APPLY_ABLE",
		allowableValues = {"BEFORE_START", "APPLY_ABLE", "RECRUITMENT_COMPLETE"})
	EnMeetingStatus status,
	@Schema(description = "모임 이미지", example = "[url 형식]")
	String imageUrl,
	@Schema(description = "모임 분류, [스터디 or 행사 or 세미나]", example = "스터디", allowableValues = {"스터디", "행사", "세미나"})
	@NotNull
	String category,
	@Schema(example = "[\n"
		+ "    \"ANDROID\",\n"
		+ "    \"IOS\"\n"
		+ "  ]", description = "대상 파트 목록")
	@NotNull
	@Size(min = 1, max = 6)
	List<MeetingJoinablePart> joinableParts,
	@Schema(description = "모임 차단 여부", example = "false")
	boolean isBlockedMeeting
) {
	public static InternalMeetingResponseDto of(Meeting meeting, LocalDateTime now, boolean isBlockedMeeting) {
		return new InternalMeetingResponseDto(meeting.getId(), meeting.getTitle(),
			meeting.getCanJoinOnlyActiveGeneration(), meeting.getMeetingStatus(now),
			meeting.getImageURL().get(0).getUrl(), meeting.getCategory().getValue(),
			Arrays.stream(meeting.getJoinableParts()).toList(),
			isBlockedMeeting);
	}
}
