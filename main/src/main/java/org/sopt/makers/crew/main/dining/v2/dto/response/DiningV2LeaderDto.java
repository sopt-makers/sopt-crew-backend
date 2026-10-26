package org.sopt.makers.crew.main.dining.v2.dto.response;

import org.sopt.makers.crew.main.entity.user.User;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "DiningV2LeaderDto", description = "회식 개설자")
public record DiningV2LeaderDto(
	@Schema(description = "개설자 크루 사용자 id", example = "1")
	@NotNull
	Integer id,

	@Schema(description = "개설자 이름", example = "홍길동")
	@NotNull
	String name,

	@Schema(description = "개설자 프로필 이미지", example = "[url] 형식")
	String profileImage
) {
	public static DiningV2LeaderDto from(User user) {
		return new DiningV2LeaderDto(user.getId(), user.getName(), user.getProfileImage());
	}
}
