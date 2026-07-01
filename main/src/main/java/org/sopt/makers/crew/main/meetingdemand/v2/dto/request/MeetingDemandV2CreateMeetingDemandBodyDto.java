package org.sopt.makers.crew.main.meetingdemand.v2.dto.request;

import java.util.List;

import org.sopt.makers.crew.main.entity.meeting.vo.MeetingJoinInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "모임 수요 생성 request body dto")
public class MeetingDemandV2CreateMeetingDemandBodyDto {

	@Schema(example = "퇴근 후 같이 러닝할 사람", description = "모임 한줄소개")
	@NotBlank
	@Size(min = 1, max = 30)
	private String shortIntro;

	@Schema(example = "혼자 뛰기는 아쉬워서 함께 꾸준히 달릴 수 있는 모임이 있으면 좋겠어요.", description = "기대하는 내용")
	@NotBlank
	@Size(min = 1, max = 1000)
	private String expectation;

	@Schema(example = """
		["운동", "네트워킹"]
		""", description = "모임 키워드 타입 리스트")
	@NotNull
	@Size(min = 1, max = 2)
	private List<String> meetingKeywordTypes;

	@Schema(example = """
		{
		  "meetingType": "오프라인",
		  "meetingFrequency": "가볍게"
		}
		""", description = "참여 정보")
	@NotNull
	private MeetingJoinInfo joinInfo;
}
