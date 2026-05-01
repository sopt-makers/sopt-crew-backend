package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "조회자와 같은 파트 참여 정보")
public record MeetingV2ParticipatingPartInfoDto(
	@Schema(description = "조회자 기준 파트", example = "서버")
	String part,
	@Schema(description = "참여중인 같은 파트 인원수", example = "3")
	int participantCount,
	@Schema(description = "활동기수 여부", example = "true")
	boolean isActiveGeneration,
	@Schema(description = "참여 정보 기준 기수", example = "38")
	Integer activeGeneration,
	@Schema(description = "조건에 맞는 신청중/대기중 유저 이름 리스트", example = "[\"이지훈\", \"김효준\"]")
	List<String> memberNames
) {
	public static MeetingV2ParticipatingPartInfoDto of(String part, int participantCount) {
		return new MeetingV2ParticipatingPartInfoDto(part, participantCount, false, null, List.of());
	}

	public static MeetingV2ParticipatingPartInfoDto of(String part, int participantCount, boolean isActiveGeneration,
		Integer activeGeneration, List<String> memberNames) {
		return new MeetingV2ParticipatingPartInfoDto(part, participantCount, isActiveGeneration, activeGeneration,
			memberNames);
	}
}
