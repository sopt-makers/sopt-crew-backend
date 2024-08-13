package org.sopt.makers.crew.main.user.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
@Schema(name = "UserV2GetAllMentionUserDto", description = "멘션 유저 조회 응답 Dto")
public class UserV2GetAllMentionUserDto {

	@Schema(description = "메이커스 프로덕트에서 범용적으로 사용하는 userId", example = "1")
	@NotNull
	private final Integer orgId;

	@Schema(description = "유저 이름", example = "홍길동")
	@NotNull
	private final String userName;

	@Schema(description = "최근 파트", example = "서버")
	@NotNull
	private final String recentPart;

	@Schema(description = "최근 기수", example = "33")
	@NotNull
	private final int recentGeneration;

	@Schema(description = "유저 프로필 사진", example = "[url] 형식")
	private final String profileImageUrl;
}

