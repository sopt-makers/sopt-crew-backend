package org.sopt.makers.crew.main.dining.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "DiningV2CreateAndUpdateRoundBodyDto", description = "회차 추가 및 수정 request body dto")
public record DiningV2CreateAndUpdateRoundBodyDto(
	@Schema(example = "비틀비틀 비틀스 타코 서순라길 점", description = "회차 장소 (선택)")
	@Size(max = 30)
	String place,

	@Schema(example = "https://naver.me/xKjC1aBc", description = "네이버지도 링크 (선택)")
	@Size(max = 500)
	String naverMapLink,

	@Schema(example = "https://kko.to/aBcDeFg", description = "카카오맵 링크 (선택)")
	@Size(max = 500)
	String kakaoMapLink,

	@Schema(example = "21:00", description = "시작 시간 (HH:mm, 선택)")
	@Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$")
	String startTime
) {
}
