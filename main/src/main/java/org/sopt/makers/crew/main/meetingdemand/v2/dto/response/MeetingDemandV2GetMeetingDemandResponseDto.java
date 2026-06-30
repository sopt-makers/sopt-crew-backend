package org.sopt.makers.crew.main.meetingdemand.v2.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.sopt.makers.crew.main.entity.meeting.vo.MeetingJoinInfo;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemand.vo.MeetingDemandAnonymousProfile;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "MeetingDemandV2GetMeetingDemandResponseDto", description = "모임 수요 조회 응답 Dto")
public class MeetingDemandV2GetMeetingDemandResponseDto {

	@Schema(description = "모임 수요 id", example = "1")
	@NotNull
	private Integer id;

	@Schema(description = "모임 한줄소개", example = "러닝 모임 열어주세요")
	@NotNull
	private String shortIntro;

	@Schema(description = "기대하는 내용", example = "함께 꾸준히 달릴 수 있는 모임이 있으면 좋겠어요.")
	@NotNull
	private String expectation;

	@Schema(description = "모임 수요 상태", example = "BEFORE_OPEN")
	@NotNull
	private String status;

	@Schema(description = "본인이 작성한 모임 수요인지 여부", example = "true")
	@NotNull
	private Boolean isMine;

	@Schema(description = "익명 닉네임", example = "성실한 판다")
	@NotNull
	private String anonymousNickname;

	@Schema(description = "익명 이미지 URL", example = "https://sopt-makers-mds.s3.ap-northeast-2.amazonaws.com/anonymousImage/avatar_m.png")
	@NotNull
	private String anonymousImageUrl;

	@Schema(description = "개설된 모임 수", example = "1")
	@NotNull
	private int openedMeetingCount;

	@Schema(description = "모임 키워드 타입 리스트", example = "[\"운동\", \"네트워킹\"]")
	@NotNull
	private List<String> meetingKeywordTypes;

	@Schema(description = "참여 정보")
	@NotNull
	private MeetingJoinInfo joinInfo;

	@Schema(description = "기다려요 수", example = "10")
	@NotNull
	private int waitCount;

	@Schema(description = "본인이 기다려요를 눌렀는지 여부", example = "true")
	@NotNull
	private Boolean isWaiting;

	@Schema(description = "댓글 수", example = "3")
	@NotNull
	private int commentCount;

	@Schema(description = "모임 수요 생성일자", example = "2026-06-30T15:30:00")
	@NotNull
	private LocalDateTime createdDate;

	public static MeetingDemandV2GetMeetingDemandResponseDto of(MeetingDemand meetingDemand, boolean isWaiting,
		boolean isMine, int openedMeetingCount) {
		return MeetingDemandV2GetMeetingDemandResponseDto.of(
			meetingDemand.getId(),
			meetingDemand.getShortIntro(),
			meetingDemand.getExpectation(),
			meetingDemand.getStatus().name(),
			isMine,
			meetingDemand.getAnonymousNickname(),
			MeetingDemandAnonymousProfile.getImageUrl(meetingDemand.getAnonymousImageNumber()),
			openedMeetingCount,
			meetingDemand.getMeetingKeywordTypes().stream()
				.map(MeetingKeywordType::getValue)
				.toList(),
			meetingDemand.getJoinInfo(),
			meetingDemand.getWaitCount(),
			isWaiting,
			meetingDemand.getCommentCount(),
			meetingDemand.createdTimestamp
		);
	}
}
