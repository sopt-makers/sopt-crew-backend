package org.sopt.makers.crew.main.common.dto;

import java.time.LocalDateTime;

import java.util.List;

import org.sopt.makers.crew.main.common.constant.CrewConst;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.user.User;

import com.querydsl.core.annotations.QueryProjection;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(name = "MeetingResponseDto", description = "모임 Dto")
@RequiredArgsConstructor
@Getter
public class MeetingResponseDto {

	@Schema(description = "모임 id", example = "1")
	@NotNull
	private final Integer id;
	@Schema(description = "모임 제목", example = "모임 제목입니다")
	@NotNull
	String title;
	@Schema(description = "대상 기수", example = "33")
	@NotNull
	private final Integer targetActiveGeneration;
	@Schema(example = "[\n"
		+ "    \"ANDROID\",\n"
		+ "    \"IOS\"\n"
		+ "  ]", description = "대상 파트 목록")
	@NotNull
	private final MeetingJoinablePart[] joinableParts;
	@Schema(example = "스터디", description = "모임 카테고리")
	@NotNull
	private final String category;
	@Schema(example = "false", description = "활동기수만 지원 가능 여부")
	@NotNull
	private final Boolean canJoinOnlyActiveGeneration;
	@Schema(example = "2", description = "모임 활동 상태")
	@NotNull
	private final Integer status;
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
	@Schema(description = "모임 활동 시작일", example = "2024-07-31T15:30:00")
	@NotNull
	private final LocalDateTime mStartDate;
	@Schema(description = "모임 활동 종료일", example = "2024-08-25T15:30:00")
	@NotNull
	private final LocalDateTime mEndDate;
	@Schema(description = "모집 인원", example = "20")
	@NotNull
	private final int capacity;
	@Schema(description = "모임장 정보", example = "")
	@NotNull
	private final MeetingCreatorDto user;
	@Schema(description = "신청 수", example = "7")
	@NotNull
	private final int appliedCount;

	@QueryProjection
	public MeetingResponseDto(Integer id, String title, Integer targetActiveGeneration,
		@NotNull MeetingJoinablePart[] joinableParts, MeetingCategory category, Boolean canJoinOnlyActiveGeneration,
		Integer status,
		List<ImageUrlVO> imageURL, Boolean isMentorNeeded, LocalDateTime mStartDate, LocalDateTime mEndDate,
		int capacity,
		MeetingCreatorDto user, int appliedCount) {

		boolean processedCanJoinOnlyActiveGeneration = canJoinOnlyActiveGeneration
			&& (CrewConst.ACTIVE_GENERATION.equals(targetActiveGeneration));

		this.id = id;
		this.title = title;
		this.targetActiveGeneration = targetActiveGeneration;
		this.joinableParts = joinableParts;
		this.category = category.getValue();
		this.canJoinOnlyActiveGeneration = processedCanJoinOnlyActiveGeneration;
		this.status = status;
		this.imageURL = imageURL;
		this.isMentorNeeded = isMentorNeeded;
		this.mStartDate = mStartDate;
		this.mEndDate = mEndDate;
		this.capacity = capacity;
		this.user = user;
		this.appliedCount = appliedCount;
	}

	public static MeetingResponseDto of(Meeting meeting, User meetingCreator, int appliedCount, LocalDateTime now) {
		MeetingCreatorDto creatorDto = MeetingCreatorDto.of(meetingCreator);
		boolean canJoinOnlyActiveGeneration = meeting.getTargetActiveGeneration() == CrewConst.ACTIVE_GENERATION
			&& meeting.getCanJoinOnlyActiveGeneration();

		return new MeetingResponseDto(meeting.getId(), meeting.getTitle(),
			meeting.getTargetActiveGeneration(), meeting.getJoinableParts(), meeting.getCategory(),
			canJoinOnlyActiveGeneration, meeting.getMeetingStatus(now), meeting.getImageURL(),
			meeting.getIsMentorNeeded(), meeting.getMStartDate(), meeting.getMEndDate(), meeting.getCapacity(),
			creatorDto, appliedCount);
	}
}
