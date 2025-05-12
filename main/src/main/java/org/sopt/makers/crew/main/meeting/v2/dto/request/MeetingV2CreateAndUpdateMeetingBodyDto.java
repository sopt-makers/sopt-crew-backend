package org.sopt.makers.crew.main.meeting.v2.dto.request;

import java.util.List;

import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "모임 생성 및 수정 request body dto")
public class MeetingV2CreateAndUpdateMeetingBodyDto {

	@Schema(example = "알고보면 쓸데있는 개발 프로세스", description = "모임 제목")
	@NotNull
	private String title;

	@Schema(example = """
		["https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2023/04/12/7bd87736-b557-4b26-a0d5-9b09f1f1d7df"]
		""", description = "모임 이미지 리스트, 최대 6개")
	@NotEmpty
	@Size(min = 1, max = 6)
	private List<String> files;

	@Schema(example = "스터디", description = "모임 카테고리")
	@NotNull
	private String category;

	@Schema(example = "2022.10.08", description = "모집 기간 시작 날짜")
	@NotNull
	private String startDate;

	@Schema(example = "2022.10.09", description = "모집 기간 끝 날짜")
	@NotNull
	private String endDate;

	@Schema(example = "5", description = "모집 인원")
	@NotNull
	private Integer capacity;

	@Schema(example = "api 가 터졌다고? 깃이 터졌다고?", description = "모집 정보")
	@NotNull
	private String desc;

	@Schema(example = "소요 시간 : 1시간 예상", description = "진행 방식 소개")
	@NotNull
	private String processDesc;

	@Schema(example = "2022.10.29", description = "모임 활동 시작 날짜", name = "mStartDate")
	@NotNull
	@Getter(AccessLevel.NONE)
	private String mStartDate;

	@Schema(example = "2022.10.30", description = "모임 활동 종료 날짜", name = "mEndDate")
	@NotNull
	@Getter(AccessLevel.NONE)
	private String mEndDate;

	@Schema(example = "안녕하세요 기획 파트 000입니다", description = "개설자 소개")
	private String leaderDesc;

	@Schema(example = "유의할 사항", description = "유의할 사항")
	private String note;

	@Schema(example = "false", description = "멘토 필요 여부")
	@NotNull
	private Boolean isMentorNeeded;

	@Schema(example = "false", description = "활동기수만 지원 가능 여부")
	@NotNull
	private Boolean canJoinOnlyActiveGeneration;

	@Schema(example = """
		["ANDROID", "IOS"]
		""", description = "대상 파트 목록")
	@NotNull
	@Size(min = 1, max = 6)
	private MeetingJoinablePart[] joinableParts;

	@Schema(example = """
		[1304, 1305]
		""", description = "공동 모임장 userId (크루에서 사용하는 userId)")
	private List<Integer> coLeaderUserIds;

	@Schema(example = """
		["YB 환영", "OB 환영"]
		""", description = "환영 메시지 타입 리스트")
	private List<String> welcomeMessageTypes;

	@Schema(example = """
		["운동", "자기계발"]
		""", description = "모임 키워드 타입 리스트")
	@Size(min = 1, max = 2)
	private List<String> meetingKeywordTypes;

	public String getmStartDate() {
		return mStartDate;
	}

	public String getmEndDate() {
		return mEndDate;
	}
}
