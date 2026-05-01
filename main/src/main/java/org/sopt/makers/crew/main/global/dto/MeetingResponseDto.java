package org.sopt.makers.crew.main.global.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2MeetingTagsResponseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(name = "MeetingResponseDto", description = "모임 Dto")
@Getter
@RequiredArgsConstructor
public class MeetingResponseDto {

	@Schema(description = "모임 id", example = "1")
	@NotNull
	private final Integer id;

	@Schema(description = "모임 제목", example = "모임 제목입니다")
	@NotNull
	private final String title;

	@Schema(description = "대상 기수", example = "33")
	@NotNull
	private final Integer targetActiveGeneration;

	@Schema(example = """
		["ANDROID", "IOS"]
		""", description = "대상 파트 목록")
	@NotNull
	private final MeetingJoinablePart[] joinableParts;

	@Schema(example = "스터디", description = "모임 카테고리")
	@NotNull
	private final String category;

	@Schema(example = "false", description = "활동기수만 지원 가능 여부")
	@NotNull
	private final Boolean canJoinOnlyActiveGeneration;

	@Schema(example = "2", description = "모임 활동 상태", type = "integer", allowableValues = {"0", "1", "2"})
	@NotNull
	private final int status;
	/**
	 * 썸네일 이미지
	 *
	 * @apiNote 여러개여도 첫번째 이미지만 사용
	 */
	@Schema(description = "모임 사진", example = "[url] 형식")
	@NotNull
	private final List<ImageUrlVO> imageURL;

	@Schema(example = "false", description = "멘토 필요 여부")
	@NotNull
	private final Boolean isMentorNeeded;

	@Schema(description = "모임 모집 시작일", example = "2024-06-11T15:30:00")
	@NotNull
	private final LocalDateTime startDate;

	@Schema(description = "모임 모집 종료일", example = "2024-06-17T15:30:00")
	@NotNull
	private final LocalDateTime endDate;

	@Schema(description = "모임 활동 시작일", example = "2024-07-31T15:30:00", name = "mStartDate")
	@Getter(AccessLevel.NONE)
	private final LocalDateTime mStartDate;

	@Schema(description = "모임 활동 종료일", example = "2024-08-25T15:30:00", name = "mEndDate")
	@Getter(AccessLevel.NONE)
	private final LocalDateTime mEndDate;

	@Schema(description = "모집 인원", example = "20")
	@NotNull
	private final int capacity;

	@Schema(description = "모임장 정보", example = "")
	@NotNull
	private final MeetingCreatorDto user;

	@Schema(description = "승인된 신청자 수", example = "3")
	@NotNull
	private final int approvedCount;

	@Schema(description = "환영 메시지 타입 목록")
	@NotNull
	private final List<String> welcomeMessageTypes;

	@Schema(description = "모임 키워드 타입 목록")
	@NotNull
	private final List<String> meetingKeywordTypes;

	public static MeetingResponseDto of(Meeting meeting, User meetingCreator, int approvedCount, LocalDateTime now,
		Integer activeGeneration, Map<Integer, TagV2MeetingTagsResponseDto> allTagsResponseDto) {

		MeetingCreatorDto creatorDto = MeetingCreatorDto.from(meetingCreator);
		boolean canJoinOnlyActiveGeneration = Objects.equals(meeting.getTargetActiveGeneration(), activeGeneration)
			&& meeting.getCanJoinOnlyActiveGeneration();

		TagV2MeetingTagsResponseDto tagInfo = allTagsResponseDto.get(meeting.getId());
		List<String> welcomeMessageTypes = extractWelcomeMessageTypes(tagInfo);
		List<String> meetingKeywordTypes = extractMeetingKeywordTypes(tagInfo);

		return new MeetingResponseDto(
			meeting.getId(),
			meeting.getTitle(),
			meeting.getTargetActiveGeneration(),
			meeting.getJoinableParts(),
			meeting.getCategory().getValue(),
			canJoinOnlyActiveGeneration,
			meeting.getMeetingStatusValue(now),
			meeting.getImageURL(),
			meeting.getIsMentorNeeded(),
			meeting.getStartDate(),
			meeting.getEndDate(),
			meeting.getmStartDate(),
			meeting.getmEndDate(),
			meeting.getCapacity(),
			creatorDto,
			approvedCount,
			welcomeMessageTypes,
			meetingKeywordTypes
		);
	}

	private static List<String> extractWelcomeMessageTypes(TagV2MeetingTagsResponseDto tagInfo) {
		if (tagInfo == null || tagInfo.welcomeMessageTypes() == null) {
			return Collections.emptyList();
		}

		return tagInfo.welcomeMessageTypes().stream()
			.map(WelcomeMessageType::getValue)
			.toList();
	}

	private static List<String> extractMeetingKeywordTypes(TagV2MeetingTagsResponseDto tagInfo) {
		if (tagInfo == null || tagInfo.meetingKeywordTypes() == null) {
			return Collections.emptyList();
		}

		return tagInfo.meetingKeywordTypes().stream()
			.map(MeetingKeywordType::getValue)
			.toList();
	}

	public LocalDateTime getmStartDate() {
		return mStartDate;
	}

	public LocalDateTime getmEndDate() {
		return mEndDate;
	}
}
