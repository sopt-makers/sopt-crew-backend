package org.sopt.makers.crew.main.user.v2.dto.response;

import org.sopt.makers.crew.main.entity.user.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UserV2GetUserOwnProfileResponseDto", description = "유저 본인 프로필 조회 응답 Dto")
public record UserV2GetUserOwnProfileResponseDto(
	@Schema(description = "유저 id, 크루에서 사용하는 userId", example = "1")
	@NotNull
	Integer id,
	@Schema(description = "org id, 메이커스 프로덕트에서 범용적으로 사용하는 userId", example = "1")
	@NotNull
	Integer orgId,
	@Schema(description = "유저 이름", example = "홍길동")
	@NotNull
	String name,
	@Schema(description = "유저 프로필 이미지", example = "[url 형식]")
	String profileImage,
	@Schema(description = "유저 활동 가지고 있는지 여부, 유저 활동이 없으면 false", example = "1")
	@NotNull
	Boolean hasActivities
) {
	public static UserV2GetUserOwnProfileResponseDto of(User user) {
		boolean hasActivities = !user.getActivities().isEmpty();

		return new UserV2GetUserOwnProfileResponseDto(user.getId(), user.getOrgId(), user.getName(),
			user.getProfileImage(), hasActivities);
	}
}
