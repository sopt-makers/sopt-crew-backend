package org.sopt.makers.crew.main.meeting.v2.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 내 같은 파트/기수 참여 멤버 리스트 조회 dto")
public record MeetingV2GetMeetingPartMembersResponseDto(
	@Schema(description = "조회자 기준 파트", example = "서버")
	String part,
	@Schema(description = "조건에 맞는 신청중/대기중 유저 수", example = "2")
	int participantCount,
	@Schema(description = "활동기수 여부", example = "true")
	boolean isActiveGeneration,
	@Schema(description = "참여 정보 기준 기수", example = "38")
	Integer activeGeneration,
	@Schema(description = "유저 리스트의 index id", example = "[1, 2]")
	List<Integer> memberIds,
	@Schema(description = "유저 이름 리스트", example = "[\"이지훈\", \"김효준\"]")
	List<String> memberNames,
	@Schema(description = "유저 프로필 이미지 리스트", example = "[\"https://example.com/profile.png\", null]")
	List<String> memberProfileImages
) {
	public static MeetingV2GetMeetingPartMembersResponseDto of(String part, int participantCount,
		boolean isActiveGeneration, Integer activeGeneration, List<Integer> memberIds, List<String> memberNames,
		List<String> memberProfileImages) {
		return new MeetingV2GetMeetingPartMembersResponseDto(part, participantCount, isActiveGeneration,
			activeGeneration, memberIds, memberNames, memberProfileImages);
	}
}
