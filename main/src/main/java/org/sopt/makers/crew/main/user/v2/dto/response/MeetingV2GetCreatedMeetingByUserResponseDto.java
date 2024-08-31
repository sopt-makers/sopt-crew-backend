package org.sopt.makers.crew.main.user.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.crew.main.common.constant.CrewConst;
import org.sopt.makers.crew.main.common.dto.MeetingCreatorDto;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.user.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MeetingV2GetCreatedMeetingByUserResponseDto", description = "모임 Dto")
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
	@Schema(example = "[\n"
		+ "    \"ANDROID\",\n"
		+ "    \"IOS\"\n"
		+ "  ]", description = "대상 파트 목록")
	@NotNull
	MeetingJoinablePart[] joinableParts,
	@Schema(example = "스터디", description = "모임 카테고리")
	@NotNull
	String category,
	@Schema(example = "false", description = "활동기수만 지원 가능 여부")
	@NotNull
	Boolean canJoinOnlyActiveGeneration,
	@Schema(example = "2", description = "모임 활동 상태")
	@NotNull
	Integer status,
	/**
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
	@Schema(description = "모임 활동 시작일", example = "2024-07-31T15:30:00")
	@NotNull
	LocalDateTime mStartDate,
	@Schema(description = "모임 활동 종료일", example = "2024-08-25T15:30:00")
	@NotNull
	LocalDateTime mEndDate,
	@Schema(description = "모집 인원", example = "20")
	@NotNull
	int capacity,
	@Schema(description = "모임장 정보", example = "")
	@NotNull
	MeetingCreatorDto user,
	@Schema(description = "신청 수", example = "7")
	@NotNull
	int appliedCount
) {
	public static MeetingV2GetCreatedMeetingByUserResponseDto of(Meeting meeting, User meetingCreator, int appliedCount, LocalDateTime now) {
		MeetingCreatorDto creatorDto = MeetingCreatorDto.of(meetingCreator);
		boolean canJoinOnlyActiveGeneration = meeting.getTargetActiveGeneration() == CrewConst.ACTIVE_GENERATION
			&& meeting.getCanJoinOnlyActiveGeneration();

		return new MeetingV2GetCreatedMeetingByUserResponseDto(meeting.getId(), meeting.getTitle(),
			meeting.getTargetActiveGeneration(), meeting.getJoinableParts(), meeting.getCategory().getValue(),
			canJoinOnlyActiveGeneration, meeting.getMeetingStatus(now), meeting.getImageURL(),
			meeting.getIsMentorNeeded(), meeting.getMStartDate(), meeting.getMEndDate(), meeting.getCapacity(), creatorDto, appliedCount);
	}

}
