package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(name = "MeetingV2GetMeetingBannerResponseDto", description = "모임 배너 응답 Dto")
public class MeetingV2GetMeetingBannerResponseDto {

	/** 모임 ID */
	@Schema(description = "모임 id", example = "1")
	@NotNull
	private final Integer id;

	/** 유저 Crew ID */
	@Schema(description = "크루에서 사용하는 userId", example = "1")
	@NotNull
	private final Integer userId;

	/** 모임 제목 */
	@Schema(description = "모임 제목", example = "모임 제목입니다1")
	@NotNull
	private final String title;

	/**
	 * 모임 카테고리
	 *
	 * @apiNote '스터디', '행사'
	 */
	@Schema(description = "모임 카테고리", example = "스터디")
	@NotNull
	private final MeetingCategory category;

	/**
	 * 썸네일 이미지
	 *
	 * @apiNote 여러개여도 첫번째 이미지만 사용
	 */
	@Schema(description = "모임 사진", example = "[url] 형식")
	@NotNull
	private final List<ImageUrlVO> imageURL;

	/** 모임 활동 시작일 */
	@Schema(description = "모임 활동 시작일", example = "2024-07-31T15:30:00")
	@NotNull
	private final LocalDateTime mStartDate;

	/** 모임 활동 종료일 */
	@Schema(description = "모임 활동 종료일", example = "2024-08-25T15:30:00")
	@NotNull
	private final LocalDateTime mEndDate;

	/** 모임 모집 시작일 */
	@Schema(description = "모임 모집 시작일", example = "2024-06-11T15:30:00")
	@NotNull
	private final LocalDateTime startDate;

	/** 모임 모집 종료일 */
	@Schema(description = "모임 모집 종료일", example = "2024-06-17T15:30:00")
	@NotNull
	private final LocalDateTime endDate;

	/** 모임 인원 */
	@Schema(description = "모집 인원", example = "20")
	@NotNull
	private final Integer capacity;

	/** 최근 활동 일자 */
	@Schema(description = "최근 활동 일자", example = "2024-06-11T15:30:00")
	private final LocalDateTime recentActivityDate;

	/** 모임 타겟 기수 */
	@Schema(description = "모임 타겟 기수", example = "33")
	@NotNull
	private final Integer targetActiveGeneration;

	/** 모임 타겟 파트 */
	@Schema(description = "모임 타겟 파트", example = "[\"PM\", \"SERVER\"]")
	@NotNull
	private final MeetingJoinablePart[] joinableParts;

	/** 지원자 수 */
	@Schema(description = "지원자 수", example = "50")
	@NotNull
	private final long applicantCount;

	/** 가입된 지원자 수 */
	@Schema(description = "가입된 지원자 수", example = "9")
	@NotNull
	private final long approvedUserCount;

	/** 개설자 정보 */
	@Schema(description = "모임장 정보", example = "")
	@NotNull
	private final MeetingV2GetMeetingBannerResponseUserDto user;

	/** 미팅 상태 */
	@Schema(description = "모임 상태", example = "1")
	@NotNull
	private final Integer status;

	public static MeetingV2GetMeetingBannerResponseDto of(Meeting meeting, LocalDateTime recentActivityDate,
		long applicantCount, long approvedUserCount, MeetingV2GetMeetingBannerResponseUserDto meetingCreator, LocalDateTime now) {

		return new MeetingV2GetMeetingBannerResponseDto(meeting.getId(), meeting.getUserId(), meeting.getTitle(),
			meeting.getCategory(),
			meeting.getImageURL(), meeting.getmStartDate(), meeting.getmEndDate(), meeting.getStartDate(),
			meeting.getEndDate(), meeting.getCapacity(), recentActivityDate, meeting.getTargetActiveGeneration(),
			meeting.getJoinableParts(), applicantCount, approvedUserCount,
			meetingCreator, meeting.getMeetingStatus(now));
	}

}
