package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 내 같은 파트 참여 멤버 리스트 조회 dto")
public record MeetingV2GetMeetingPartMembersResponseDto(
	@Schema(description = "조회자 기준 파트", example = "서버")
	String part,
	@Schema(description = "참여중인 같은 파트 인원수", example = "2")
	int participantCount,
	@Schema(description = "참여중인 같은 파트 멤버 이름 리스트", example = "[\"이지훈\", \"김효준\"]")
	List<String> memberNames
) {
	public static MeetingV2GetMeetingPartMembersResponseDto of(String part, int participantCount,
		List<String> memberNames) {
		return new MeetingV2GetMeetingPartMembersResponseDto(part, participantCount, memberNames);
	}
}
