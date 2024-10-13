package org.sopt.makers.crew.main.user.v2.dto.response;

import org.sopt.makers.crew.main.entity.user.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "UserV2GetAllUserDto", description = "전체 사용자 조회 응답 Dto")
public record UserV2GetAllUserDto(
	@Schema(description = "크루에서 사용하는 userId", example = "103")
	@NotNull
	Integer userId,
	@Schema(description = "메이커스 프로덕트에서 범용적으로 사용하는 userId", example = "403")
	@NotNull
	Integer orgId,

	@Schema(description = "유저 이름", example = "홍길동")
	@NotNull
	String userName,

	@Schema(description = "최근 파트", example = "서버")
	@NotNull
	String recentPart,

	@Schema(description = "최근 기수", example = "33")
	@NotNull
	int recentGeneration,

	@Schema(description = "유저 프로필 사진", example = "[url] 형식")
	String profileImageUrl
) {
	public static UserV2GetAllUserDto of(User user) {
		return new UserV2GetAllUserDto(user.getId(), user.getOrgId(), user.getName(),
			user.getRecentActivityVO().getPart(), user.getRecentActivityVO().getGeneration(), user.getProfileImage());
	}
}
