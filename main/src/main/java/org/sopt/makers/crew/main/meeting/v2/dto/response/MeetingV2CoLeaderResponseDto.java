package org.sopt.makers.crew.main.meeting.v2.dto.response;

import org.sopt.makers.crew.main.entity.user.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "공동 모임장 조회 dto")
public record MeetingV2CoLeaderResponseDto(
	@Schema(description = "공동 모임장 id, 크루에서 사용하는 userId", example = "203")
	@NotNull
	Integer userId,
	@Schema(description = "공동 모임장 org id, 메이커스 프로덕트에서 범용적으로 사용하는 userId", example = "789")
	@NotNull
	Integer orgId,
	@Schema(description = "공동 모임장 이름", example = "공동 모임장 이름")
	@NotNull
	String userName,
	@Schema(description = "공동 모임장 프로필 이미지 url", example = "[url 형식]")
	@NotNull
	String userprofileImage
) {
	public static MeetingV2CoLeaderResponseDto of(User user) {
		return new MeetingV2CoLeaderResponseDto(user.getId(), user.getOrgId(), user.getName(),
			user.getProfileImage());
	}
}
