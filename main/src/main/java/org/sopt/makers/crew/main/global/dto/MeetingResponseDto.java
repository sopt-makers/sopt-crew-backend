package org.sopt.makers.crew.main.global.dto;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Objects;

import org.sopt.makers.crew.main.global.constant.CrewConst;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.user.User;

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
	private final String title;
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
	@Schema(description = "모임 활동 시작일", example = "2024-07-31T15:30:00", name = "mStartDate")
	@NotNull
	private final LocalDateTime mStartDate;
	@Schema(description = "모임 활동 종료일", example = "2024-08-25T15:30:00", name = "mEndDate")
	@NotNull
	private final LocalDateTime mEndDate;
	@Schema(description = "모집 인원", example = "20")
	@NotNull
	private final int capacity;
	@Schema(description = "모임장 정보", example = "")
	@NotNull
	private final MeetingCreatorDto user;
	@Schema(description = "TODO: FE에서 수정 완료 후 삭제 ", example = "[DEPRECATED]")
	@NotNull
	private final int appliedCount;
	@Schema(description = "승인된 신청자 수", example = "3")
	@NotNull
	private final int approvedCount;

	public LocalDateTime getmStartDate() {
		return mStartDate;
	}

	public LocalDateTime getmEndDate() {
		return mEndDate;
	}

	public static MeetingResponseDto of(Meeting meeting, User meetingCreator, int approvedCount, LocalDateTime now) {
		MeetingCreatorDto creatorDto = MeetingCreatorDto.of(meetingCreator);
		boolean canJoinOnlyActiveGeneration =
			Objects.equals(meeting.getTargetActiveGeneration(), CrewConst.ACTIVE_GENERATION)
			&& meeting.getCanJoinOnlyActiveGeneration();

		return new MeetingResponseDto(meeting.getId(), meeting.getTitle(),
			meeting.getTargetActiveGeneration(), meeting.getJoinableParts(), meeting.getCategory().getValue(),
			canJoinOnlyActiveGeneration, meeting.getMeetingStatus(now), meeting.getImageURL(),
			meeting.getIsMentorNeeded(), meeting.getmStartDate(), meeting.getmEndDate(), meeting.getCapacity(),
			creatorDto, approvedCount, approvedCount);
	}
}
