package org.sopt.makers.crew.main.meeting.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "조회자와 같은 파트 참여 정보")
public record MeetingV2ParticipatingPartInfoDto(
	@Schema(description = "조회자 기준 파트", example = "서버")
	String part,
	@Schema(description = "참여중인 같은 파트 인원수", example = "3")
	int participantCount
) {
	public static MeetingV2ParticipatingPartInfoDto of(String part, int participantCount) {
		return new MeetingV2ParticipatingPartInfoDto(part, participantCount);
	}
}
