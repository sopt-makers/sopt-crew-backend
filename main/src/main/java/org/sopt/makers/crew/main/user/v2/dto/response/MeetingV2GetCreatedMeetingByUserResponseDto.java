package org.sopt.makers.crew.main.user.v2.dto.response;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.tag.enums.WelcomeMessageType;
import org.sopt.makers.crew.main.global.dto.MeetingCreatorDto;
import org.sopt.makers.crew.main.tag.v2.dto.response.TagV2MeetingTagsResponseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Schema(name = "MeetingV2GetCreatedMeetingByUserResponseDto", description = "모임 Dto")
@Builder(access = AccessLevel.PRIVATE)
public record MeetingV2GetCreatedMeetingByUserResponseDto(
	@Schema(description = "모임 id", example = "1")
	@NotNull
	Integer id,

	@Schema(description = "모임 제목", example = "모임 제목입니다")
	@NotNull
	String title,

	@Schema(description = "대상 기수", example = "33")
	@NotNull
	Integer targetActiveGeneration,

	@Schema(example = """
		["ANDROID", "IOS"]
		""", description = "대상 파트 목록")
	@NotNull
	MeetingJoinablePart[] joinableParts,

	@Schema(example = "스터디", description = "모임 카테고리")
	@NotNull
	String category,

	@Schema(example = "false", description = "활동기수만 지원 가능 여부")
	@NotNull
	Boolean canJoinOnlyActiveGeneration,

	@Schema(example = "2", description = "모임 활동 상태", type = "integer", allowableValues = {"0", "1", "2"})
	@NotNull
	int status,

	@Schema(example = "false", description = "공동 모임장 여부")
	@NotNull
	@Getter(AccessLevel.NONE)
	boolean isCoLeader,
	/*
	 * 썸네일 이미지
	 *
	 * @apiNote 여러개여도 첫번째 이미지만 사용
	 */
	@Schema(description = "모임 사진", example = "[url] 형식")
	@NotNull
	List<ImageUrlVO> imageURL,

	@Schema(example = "false", description = "멘토 필요 여부")
	@NotNull
	Boolean isMentorNeeded,

	@Schema(description = "모임 모집 시작일", example = "2024-06-11T15:30:00")
	@NotNull
	LocalDateTime startDate,

	@Schema(description = "모임 모집 종료일", example = "2024-06-17T15:30:00")
	@NotNull
	LocalDateTime endDate,

	@Schema(description = "모임 활동 시작일", example = "2024-07-31T15:30:00")
	@NotNull
	@Getter(AccessLevel.NONE)
	LocalDateTime mStartDate,

	@Schema(description = "모임 활동 종료일", example = "2024-08-25T15:30:00")
	@NotNull
	@Getter(AccessLevel.NONE)
	LocalDateTime mEndDate,

	@Schema(description = "모집 인원", example = "20")
	@NotNull
	int capacity,

	@Schema(description = "모임장 정보", example = "")
	@NotNull
	MeetingCreatorDto user,

	@Schema(description = "신청자 수", example = "7")
	@NotNull
	int approvedCount,

	@Schema(description = "환영 메시지 타입 목록")
	@NotNull
	List<String> welcomeMessageTypes,

	@Schema(description = "모임 키워드 타입 목록")
	@NotNull
	List<String> meetingKeywordTypes
) {
	public static MeetingV2GetCreatedMeetingByUserResponseDto of(Meeting meeting, boolean isCoLeader, int approvedCount,
		LocalDateTime now, Integer activeGeneration, Map<Integer, TagV2MeetingTagsResponseDto> allTagsResponseDto) {

		MeetingCreatorDto creatorDto = MeetingCreatorDto.from(meeting.getUser());
		boolean canJoinOnlyActiveGeneration = Objects.equals(meeting.getTargetActiveGeneration(), activeGeneration)
			&& meeting.getCanJoinOnlyActiveGeneration();

		TagV2MeetingTagsResponseDto tagInfo = allTagsResponseDto.get(meeting.getId());
		List<String> welcomeMessageTypes = extractWelcomeMessageTypes(tagInfo);
		List<String> meetingKeywordTypes = extractMeetingKeywordTypes(tagInfo);

		return MeetingV2GetCreatedMeetingByUserResponseDto.builder()
			.id(meeting.getId())
			.title(meeting.getTitle())
			.targetActiveGeneration(meeting.getTargetActiveGeneration())
			.joinableParts(meeting.getJoinableParts())
			.category(meeting.getCategory().getValue())
			.canJoinOnlyActiveGeneration(canJoinOnlyActiveGeneration)
			.status(meeting.getMeetingStatusValue(now))
			.isCoLeader(isCoLeader)
			.imageURL(meeting.getImageURL())
			.isMentorNeeded(meeting.getIsMentorNeeded())
			.startDate(meeting.getStartDate())
			.endDate(meeting.getEndDate())
			.mStartDate(meeting.getmStartDate())
			.mEndDate(meeting.getmEndDate())
			.capacity(meeting.getCapacity())
			.user(creatorDto)
			.approvedCount(approvedCount)
			.welcomeMessageTypes(welcomeMessageTypes)
			.meetingKeywordTypes(meetingKeywordTypes)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		MeetingV2GetCreatedMeetingByUserResponseDto that = (MeetingV2GetCreatedMeetingByUserResponseDto)o;
		return status == that.status &&
			isCoLeader == that.isCoLeader &&
			capacity == that.capacity &&
			approvedCount == that.approvedCount &&
			Objects.equals(id, that.id) &&
			Objects.equals(title, that.title) &&
			Objects.equals(targetActiveGeneration, that.targetActiveGeneration) &&
			Arrays.equals(joinableParts, that.joinableParts) &&
			Objects.equals(category, that.category) &&
			Objects.equals(canJoinOnlyActiveGeneration, that.canJoinOnlyActiveGeneration) &&
			Objects.equals(imageURL, that.imageURL) &&
			Objects.equals(isMentorNeeded, that.isMentorNeeded) &&
			Objects.equals(mStartDate, that.mStartDate) &&
			Objects.equals(mEndDate, that.mEndDate) &&
			Objects.equals(user, that.user) &&
			Objects.equals(welcomeMessageTypes, that.welcomeMessageTypes) &&
			Objects.equals(meetingKeywordTypes, that.meetingKeywordTypes);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(
			id,
			title,
			targetActiveGeneration,
			category,
			canJoinOnlyActiveGeneration,
			status,
			isCoLeader,
			imageURL,
			isMentorNeeded,
			mStartDate,
			mEndDate,
			capacity,
			user,
			approvedCount,
			welcomeMessageTypes,
			meetingKeywordTypes);
		result = 31 * result + Arrays.hashCode(joinableParts);
		return result;
	}

	@Override
	public String toString() {
		return "MeetingV2GetCreatedMeetingByUserResponseDto[" +
			"id=" + id + ", " +
			"title=" + title + ", " +
			"targetActiveGeneration=" + targetActiveGeneration + ", " +
			"joinableParts=" + Arrays.toString(joinableParts) + ", " +
			"category=" + category + ", " +
			"canJoinOnlyActiveGeneration=" + canJoinOnlyActiveGeneration + ", " +
			"status=" + status + ", " +
			"isCoLeader=" + isCoLeader + ", " +
			"imageURL=" + imageURL + ", " +
			"isMentorNeeded=" + isMentorNeeded + ", " +
			"mStartDate=" + mStartDate + ", " +
			"mEndDate=" + mEndDate + ", " +
			"capacity=" + capacity + ", " +
			"user=" + user + ", " +
			"approvedCount=" + approvedCount + ", " +
			"welcomeMessageTypes=" + welcomeMessageTypes + ", " +
			"meetingKeywordTypes=" + meetingKeywordTypes + "]";
	}

	public boolean getIsCoLeader() {
		return isCoLeader;
	}

	public LocalDateTime getmStartDate() {
		return mStartDate;
	}

	public LocalDateTime getmEndDate() {
		return mEndDate;
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
}
