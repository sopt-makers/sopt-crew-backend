package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.crew.main.common.dto.MeetingCreatorDto;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.user.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "모임 상세 조회 dto")
public class MeetingV2GetMeetingByIdResponseDto {

	@Schema(description = "모임 id", example = "2")
	@NotNull
	private final Integer id;

	@Schema(description = "모임장 id", example = "184")
	@NotNull
	private final Integer userId;

	@Schema(description = "모임 제목", example = "모임 제목입니다.")
	@NotNull
	private final String title;

	@Schema(description = "모임 카테고리", example = "스터디")
	@NotNull
	private final String category;

	@Schema(description = "모임 이미지", example = "[url 형식]")
	@NotNull
	private final List<ImageUrlVO> imageURL;

	@Schema(description = "모임 신청 시작 시간", example = "2024-07-10T15:30:00")
	@NotNull
	private final LocalDateTime startDate;

	@Schema(description = "모임 신청 종료 시간", example = "2024-07-30T23:59:59")
	@NotNull
	private final LocalDateTime endDate;

	@Schema(description = "모집 인원", example = "23")
	@NotNull
	private final int capacity;

	@Schema(description = "모임 소개", example = "모임 소개 입니다.")
	@NotNull
	private final String desc;

	@Schema(description = "진행방식 소개", example = "진행방식 설명입니다.")
	@NotNull
	private final String processDesc;

	@Schema(description = "모임 활동 시작 시간", example = "2024-08-13T15:30:00")
	@NotNull
	private final LocalDateTime mStartDate;

	@Schema(description = "모임 활동 종료 시간", example = "2024-10-13T23:59:59")
	@NotNull
	private final LocalDateTime mEndDate;

	@Schema(description = "개설자 소개", example = "개설자 소개 입니다.")
	@NotNull
	private final String leaderDesc;

	@Schema(description = "모집 대상 소개", example = "모집 대상 소개입니다.")
	@NotNull
	private final String targetDesc;

	@Schema(description = "유의사항", example = "유의사항입니다.")
	@NotNull
	private final String note;

	@Schema(description = "멘토 필요 여부", example = "true")
	@NotNull
	private final Boolean isMentorNeeded;

	@Schema(description = "활동 기수만 신청가능한 여부", example = "false")
	@NotNull
	private final Boolean canJoinOnlyActiveGeneration;

	@Schema(description = "개설 기수", example = "36")
	@NotNull
	private final Integer createdGeneration;

	@Schema(description = "모집 대상 기수", example = "36")
	@NotNull
	private final Integer targetActiveGeneration;

	@Schema(description = "모집 대상 파트", example = "[\n"
		+ "            \"PM\",\n"
		+ "            \"DESIGN\",\n"
		+ "            \"WEB\",\n"
		+ "            \"ANDROID\",\n"
		+ "            \"IOS\",\n"
		+ "            \"SERVER\"\n"
		+ "        ]")
	@NotNull
	private final MeetingJoinablePart[] joinableParts;

	@Schema(description = "모임 상태, 0: 모집전, 1: 모집중, 2: 모집종료", example = "1")
	@NotNull
	private final Integer status;

	@Schema(description = "승인된 신청 수", example = "7")
	@NotNull
	private final long approvedApplyCount;

	@Schema(description = "모임 개설자 여부", example = "true")
	@NotNull
	private final Boolean host;

	@Schema(description = "모임 신청 여부", example = "false")
	@NotNull
	private final Boolean apply;

	@Schema(description = "모임 승인 여부", example = "false")
	@NotNull
	private final Boolean approved;

	@Schema(description = "모임장 객체", example = "")
	@NotNull
	private final MeetingCreatorDto user;

	@Schema(description = "신청 목록", example = "")
	@NotNull
	private final List<ApplyWholeInfoDto> appliedInfo;

	public static MeetingV2GetMeetingByIdResponseDto of(Meeting meeting, long approvedCount, Boolean isHost, Boolean isApply,
		Boolean isApproved, User meetingCreator,
		List<ApplyWholeInfoDto> appliedInfo, LocalDateTime now) {

		MeetingCreatorDto meetingCreatorDto = MeetingCreatorDto.of(meetingCreator);

		Integer meetingStatus = meeting.getMeetingStatus(now);

		return new MeetingV2GetMeetingByIdResponseDto(meeting.getId(), meeting.getUserId(), meeting.getTitle(),
			meeting.getCategory().getValue(), meeting.getImageURL(), meeting.getStartDate(), meeting.getEndDate(),
			meeting.getCapacity(), meeting.getDesc(), meeting.getProcessDesc(), meeting.getMStartDate(),
			meeting.getMEndDate(), meeting.getLeaderDesc(), meeting.getTargetDesc(), meeting.getNote(),
			meeting.getIsMentorNeeded(), meeting.getCanJoinOnlyActiveGeneration(), meeting.getCreatedGeneration(),
			meeting.getTargetActiveGeneration(), meeting.getJoinableParts(), meetingStatus,
			approvedCount, isHost, isApply, isApproved, meetingCreatorDto, appliedInfo);
	}

	public LocalDateTime getmStartDate() {
		return mStartDate;
	}

	public LocalDateTime getmEndDate() {
		return mEndDate;
	}
}
