package org.sopt.makers.crew.main.user.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.global.dto.MeetingCreatorDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;

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
	@Schema(example = "2", description = "모임 활동 상태", type = "integer", allowableValues = {"0", "1", "2"})
	@NotNull
	int status,
	@Schema(example = "false", description = "공동 모임장 여부")
	@NotNull
	@Getter(AccessLevel.NONE)
	boolean isCoLeader,
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
	@Schema(description = "[DEPRECATED] TODO: FE에서 수정 완료 후 삭제 ", example = "7")
	@NotNull
	int appliedCount,
	@Schema(description = "신청자 수", example = "7")
	@NotNull
	int approvedCount
) {
	public static MeetingV2GetCreatedMeetingByUserResponseDto of(Meeting meeting, boolean isCoLeader, int approvedCount,
		LocalDateTime now, Integer activeGeneration) {
		MeetingCreatorDto creatorDto = MeetingCreatorDto.of(meeting.getUser());
		boolean canJoinOnlyActiveGeneration = Objects.equals(meeting.getTargetActiveGeneration(), activeGeneration)
			&& meeting.getCanJoinOnlyActiveGeneration();

		return new MeetingV2GetCreatedMeetingByUserResponseDto(meeting.getId(), meeting.getTitle(),
			meeting.getTargetActiveGeneration(), meeting.getJoinableParts(), meeting.getCategory().getValue(),
			canJoinOnlyActiveGeneration, meeting.getMeetingStatusValue(now), isCoLeader, meeting.getImageURL(),
			meeting.getIsMentorNeeded(), meeting.getmStartDate(), meeting.getmEndDate(), meeting.getCapacity(),
			creatorDto, approvedCount, approvedCount);
	}

}
